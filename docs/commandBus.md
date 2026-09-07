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
conservent leur `HttpStatusCode`, toute autre `RuntimeException` devient `INTERNAL_ERROR_500`. Au-delà
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
  `uncommittedEvents` est **capturé sur l'agrégat par la fabrique** `of(...)` : un instantané figé au
  retour du handler.
- `ExecutionResult` (infra) — interface **sealed** `permits SuccessResult, ErrorResult`, ce que
  retourne le bus. Le pattern matching exhaustif sur `switch` dans les middlewares dépend de ce
  scellement : ajouter une implémentation casse tous les `switch` existants (volontairement).

`isSuccess()` n'est pas un composant de record mais une **constante par implémentation** : `true` sur
`SuccessResult`, `false` sur `ErrorResult`. Il ne se renseigne donc pas à la construction, et aucune
fabrique ne peut produire un résultat incohérent avec son type.

`SuccessResult` porte `commandId`, `userId`, `executedOn`, l'`aggregate` et la liste
`uncommittedEvents`. L'entité y est transmise entière, identité comprise : c'est à l'appelant d'en
déballer ce dont il a besoin (`s.aggregate().id().value()`).

## Passage du domaine à l'infra

`Dispatcher` est le point de traduction `CommandResult` → `SuccessResult`, par `switch` exhaustif sur
la hiérarchie scellée :

```java
return switch (commandResult) {
    case DDDEntityResult r    -> SuccessResult.of(appCommand, r);
    case AggregateRootResult r -> SuccessResult.of(appCommand, r, r.uncommittedEvents());
};
```

La fabrique à deux arguments force `uncommittedEvents` à `List.of()` : un référentiel modélisé en
`DDDEntity` ne remonte jamais d'événements.

`DomainEventPublisherMiddleware` lit `SuccessResult.uncommittedEvents()` et sort immédiatement sur
liste vide. Il ne consulte jamais l'agrégat : la liste figée par le `Dispatcher` est sa seule source.

La publication ne solde pas les événements : `AggregateRoot.resetEvents()` n'est appelé par aucun
middleware, le moment revient à l'adaptateur de persistance ou à l'agrégat lui-même.
