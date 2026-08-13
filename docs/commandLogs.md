# Journal des commandes

Traçabilité des commandes exécutées par le bus, écrite par `LoggingMiddleware` via le port
`AppCommandRepository`.

Ce journal relève de l'**observabilité**, pas de la conformité : il sert à comprendre ce que
l'application a fait, pas à en fournir la preuve. Tous les arbitrages ci-dessous en découlent —
notamment le fait qu'une trace perdue soit un incident d'exploitation, pas une faute.

## Cycle de vie d'une trace

`AppCommand` est un record immuable suivant le cycle `START(command, userId, endpoint)` →
`withResult(executionResult)`, ce dernier produisant une nouvelle instance en phase `COMPLETED`,
enrichie de `completedOn`, `isSuccess`, `codeException` et `error`. `withResult` fait partie du
contrat `ApplicationCommand`, car `LoggingMiddleware` ne manipule que l'interface.

**`LoggingMiddleware` sauvegarde deux fois** : l'instance `START` *avant* `next.handle()`, puis
l'instance résultat *après*. Une commande dont l'exécution ne revient jamais (`Error`, blocage, arrêt
brutal) laisse donc quand même sa trace de réception.

## Append-only

Chaque `save` insère un nouvel enregistrement, discriminé par `CommandPhase` (`RECEIVED` /
`COMPLETED`). L'`id` de commande est donc partagé par deux lignes et n'est pas une clé primaire
utilisable seule. Ce choix supprime toute exigence d'idempotence *et* d'ordonnancement — une
implémentation asynchrone de `AppCommandRepository` est correcte sans précaution particulière. Le
contrat complet est en javadoc sur `AppCommandRepository`.

## Horodatage

`completedOn` n'est renseigné que sur la trace `COMPLETED`, avec un `LocalDateTime.now()` capturé une
seule fois dans `withResult` — donc *après* le commit, et de sémantique identique quelle que soit
l'issue. Ne pas le remplacer par `ExecutionResult.executedOn()` : côté succès celui-ci remonte du
handler, avant commit, et côté erreur de la conversion d'exception. La durée de traitement se calcule
par `completedOn - receivedOn` sur la seule ligne `COMPLETED`.

## Isolation des pannes

Les deux appels passent par `saveQuietly`, qui avale toute `RuntimeException` et journalise en
`ERROR`. `LoggingMiddleware` étant le middleware le plus externe (voir
[commandBus.md](commandBus.md)), une exception du journal ne serait rattrapée par personne ; et comme
le second `save` suit le commit, la laisser remonter signalerait un échec sur une commande déjà
appliquée — l'appelant rejouerait un effet métier acquis. Le journal est donc volontairement
*best-effort*, et une panne de sa base est silencieuse côté appelant : elle ne se voit que dans les
logs.
