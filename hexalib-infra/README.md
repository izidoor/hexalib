# hexalib-infra

Implémentation Spring de [`hexalib-domain`](../hexalib-domain) : bus de commandes, transactions,
publication d'événements, enregistrement automatique des beans de domaine.

Dépend de `spring-context` et `spring-tx` (versions alignées sur le BOM Spring Boot 4), Jackson 3,
SLF4J et Lombok. Spring Boot n'est pas requis à l'exécution : tout contexte Spring fournissant un
`PlatformTransactionManager` convient.

## Activation

```java
@SpringBootApplication
@EnableCqrsInfra(domainBasePackage = "com.exemple.app.domain")
public class Application { }
```

`domainBasePackage` est scanné pour `@CommandHandlerComponent`, `@DomainEventListenerComponent`,
`@DomainEventPublisherComponent` et `@DomainService`. Une classe non annotée, ou hors de ce package,
n'est pas enregistrée.

L'application doit fournir :

- un `PlatformTransactionManager` ;
- une implémentation de `AppCommandRepository` (journal des commandes) — aucune n'est livrée. Son
  contrat est documenté en javadoc sur l'interface ; voir [Journal des commandes](#journal-des-commandes).

### Stubs

```properties
fr.izidor.hexalib.infra.stubs.enabled=true
```

Enregistre en plus les classes `@Stub` du même package. À réserver aux profils de test et de dev.

## Envoi d'une commande

```java
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CommandBus commandBus;

    @PostMapping("/customers/{id}/email")
    public ResponseEntity<?> changeEmail(@PathVariable String id, @RequestBody EmailRequest body) {

        var result = commandBus.handle(
                AppCommand.INIT(new ChangeEmail(id, body.email()), currentUserId(), "POST /customers/{id}/email"));

        return switch (result) {
            case SuccessResult s -> ResponseEntity.ok(s.aggregate());
            case ErrorResult e -> ResponseEntity.status(toHttpStatus(e.codeException())).body(e.error());
        };
    }
}
```

`ExecutionResult` est une interface `sealed` : le `switch` est exhaustif sans `default`. Le bus ne
lève pas d'exception, toute erreur revient en `ErrorResult`.

## Chaîne de middlewares

```
CommandBus
  └─ LoggingMiddleware               chrono + sauvegarde de l'ApplicationCommand
      └─ DomainEventPublisherMiddleware   publication vers les DomainEventListener
          └─ UnitOfWorkMiddleware          TransactionTemplate + conversion des exceptions
              └─ Dispatcher                appel du CommandHandler
```

L'ordre est fixé dans le constructeur de `CommandBus`, les middlewares ne sont pas des beans.

Deux conséquences :

- **les événements de domaine sont publiés après le commit** — l'échec d'un listener n'annule pas la
  transaction ;
- le journal des commandes est écrit hors transaction, avant et après l'exécution, donc conservé même
  en cas d'échec ou d'interruption.

`UnitOfWorkMiddleware` est le seul point de conversion exception → résultat : une `AggregatException`
conserve son `CodeException`, toute autre `RuntimeException` devient `INTERNAL_ERROR_500`.

Les événements publiés sont ceux que porte `SuccessResult.uncommittedEvents()`. Cette liste est
remplie par le `Dispatcher`, qui distingue par `switch` les deux variantes de `CommandResult` : un
`AggregateRootResult` relaie ses événements, un `DDDEntityResult` produit `List.of()`. Le middleware
de publication sort immédiatement sur liste vide et ne consulte jamais l'agrégat — voir
[commandBus.md](../docs/commandBus.md).

## Journal des commandes

Ce journal relève de l'**observabilité**, pas de la conformité : il sert à comprendre ce que
l'application a fait, pas à en apporter la preuve. C'est ce qui justifie son caractère best-effort
décrit plus bas.

`AppCommandRepository.save` est appelé **deux fois par commande** : à la réception, puis une fois
l'exécution terminée. Les deux appels portent le même `id`, discriminés par `CommandPhase` :

| `phase` | `completedOn` | `isSuccess` / `codeException` / `error` |
| --- | --- | --- |
| `RECEIVED` | `null` | `null` |
| `COMPLETED` | renseigné | renseignés |

`receivedOn` est porté par les deux traces, `completedOn` par la seule `COMPLETED` : la durée de
traitement se calcule par différence entre les deux colonnes d'une même ligne `COMPLETED`.

Le journal est **append-only** : chaque appel doit insérer un nouvel enregistrement, jamais mettre à
jour le précédent. L'`id` de commande n'est donc pas une clé primaire utilisable seule — la combiner
avec `phase`, ou utiliser une clé technique.

Ce choix a une conséquence pratique : aucune idempotence n'est requise, aucun ordre d'arrivée n'est
imposé, et une trace `COMPLETED` écrite avant sa `RECEIVED` reste correcte. **Une implémentation
asynchrone est donc correcte sans précaution d'ordonnancement.**

### Isolation des pannes du journal

`LoggingMiddleware` étant le middleware le plus externe, une exception de `AppCommandRepository`
remonterait jusqu'à l'appelant sans être rattrapée. Les deux `save` sont donc encapsulés dans un
`saveQuietly` qui journalise en `ERROR` et poursuit.

C'est délibéré : le `save` de sortie a lieu **après le commit** de la transaction métier. Le laisser
échouer signalerait un échec à l'appelant pour une commande pourtant appliquée, avec un rejeu à la
clé — une panne d'observabilité deviendrait une corruption de données métier.

Conséquence opérationnelle : une base injoignable est **invisible depuis l'extérieur**. Prévoir une
alerte sur ce message d'erreur, sinon des journées de traces disparaissent sans signal. Le message
contient `commandName`, `id` et `userId` pour permettre le rattrapage.

Une implémentation **asynchrone** échappe à ce filet : l'échec survient après le retour de `save`,
sur un autre thread. Elle reprend alors à sa charge la journalisation contextualisée des échecs.

## Résolution des handlers

`Dispatcher` et `DomainEventPublisherMiddleware` comparent les classes par égalité stricte
(`listenToCommand()`, `listenToEvent()`), sans prise en compte de l'héritage. Un handler déclaré sur
une super-classe ne recevra pas ses sous-types. Une commande sans handler produit un
`INTERNAL_ERROR_500`.
