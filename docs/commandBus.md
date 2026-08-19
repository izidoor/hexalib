# Bus de commandes

Chaîne d'exécution d'une commande dans `hexalib-infra`, du point d'entrée applicatif jusqu'au
`CommandHandler` du domaine, et modèle de résultat associé.

## Chaîne de middlewares

`CommandBus` est le seul `@Component` de la chaîne : il **assemble manuellement** les middlewares dans
son constructeur (ils ne sont ni des beans, ni injectés). L'ordre est donc figé dans le code :

```
CommandBus
  └─ LoggingMiddleware              chrono + persistance de l'ApplicationCommand
      └─ DomainEventPublisherMiddleware   publication des événements de domaine
          └─ UnitOfWorkMiddleware         TransactionTemplate + conversion des exceptions
              └─ Dispatcher               résolution et appel du CommandHandler
```

Deux conséquences à garder en tête avant de modifier l'ordre :

1. **Les événements de domaine sont publiés après le commit**, puisque `DomainEventPublisherMiddleware`
   enveloppe `UnitOfWorkMiddleware`. Un listener qui échoue ne provoque donc pas de rollback.
2. `LoggingMiddleware` sauvegarde l'`ApplicationCommand` hors transaction, ce qui préserve la trace
   d'observabilité même en cas d'échec. Voir [commandLogs.md](commandLogs.md).

`UnitOfWorkMiddleware` est le point unique de conversion exception → résultat : les `AggregatException`
conservent leur `CodeException`, toute autre `RuntimeException` devient `INTERNAL_ERROR_500`. Au-delà
de ce middleware, plus rien ne remonte sous forme d'exception.

## Résolution par égalité stricte de classe

`Dispatcher` et `DomainEventPublisherMiddleware` sélectionnent leurs cibles par `equals` sur la classe
(`listenToCommand()`, `listenToEvent()`), pas par assignabilité. Un handler enregistré pour une
super-classe ne recevra donc jamais une sous-classe. Un dispatch sans handler lève une
`IllegalArgumentException` (attrapée en amont par `UnitOfWorkMiddleware` → 500).

## Modèle de résultat

Deux niveaux distincts, à ne pas confondre :

- `CommandResult` (domaine) — ce que retourne un `CommandHandler`. **Scellée** :
  `permits AggregateRootResult, DDDEntityResult`. Son axe de variation est la présence d'événements,
  pas le succès : le domaine n'a pas de variante d'échec, les erreurs voyagent en exception jusqu'à
  `UnitOfWorkMiddleware`. L'interface ne déclare que le strict commun — `executedOn()` et `entity()` ;
  le transport des événements appartient à la seule variante `AggregateRootResult`, dont le composant
  `uncommittedEvents` est **capturé sur l'agrégat par la fabrique** `of(...)`. C'est un instantané pris
  au retour du handler : muter l'agrégat après coup ne changera plus ce que le bus publiera.
- `ExecutionResult` (infra) — interface **sealed** `permits SuccessResult, ErrorResult`, ce que
  retourne le bus. Le pattern matching exhaustif sur `switch` dans les middlewares dépend de ce
  scellement : ajouter une implémentation casse tous les `switch` existants (volontairement).

`isSuccess()` n'est pas un composant de record mais une **constante par implémentation** : `true` sur
`SuccessResult`, `false` sur `ErrorResult`. Il ne se renseigne donc pas à la construction, et aucune
fabrique ne peut produire un résultat incohérent avec son type.

`SuccessResult` porte `commandId`, `userId`, `executedOn`, l'`aggregate` et la liste
`uncommittedEvents`. Le value object d'identité n'est pas déballé : l'entité complète est transmise,
et c'est à l'appelant d'en tirer ce dont il a besoin (`s.aggregate().id().value()`).

## Passage du domaine à l'infra

`Dispatcher` est le point de traduction `CommandResult` → `SuccessResult`, par `switch` exhaustif sur
la hiérarchie scellée :

```java
return switch (commandResult) {
    case DDDEntityResult r    -> SuccessResult.of(appCommand, r);
    case AggregateRootResult r -> SuccessResult.of(appCommand, r, r.uncommittedEvents());
};
```

Deux fabriques `SuccessResult.of(...)` plutôt qu'une seule : la variante à deux arguments force
`uncommittedEvents` à `List.of()`. Un référentiel modélisé en `DDDEntity` ne peut donc pas remonter
d'événements, et le middleware de publication n'a pas de cas particulier à traiter pour lui.

`DomainEventPublisherMiddleware` lit `SuccessResult.uncommittedEvents()` et **retourne immédiatement
si la liste est vide**, avant même de parcourir les listeners. La liste est celle figée par le
`Dispatcher` : le middleware ne consulte jamais l'agrégat.

À noter : la publication ne solde pas les événements. `AggregateRoot.resetEvents()` existe mais
n'est appelé nulle part dans la chaîne — c'est à l'adaptateur de persistance ou à l'agrégat lui-même
d'en décider.
