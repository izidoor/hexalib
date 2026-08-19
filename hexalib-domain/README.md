# hexalib-domain

Abstractions DDD et CQRS, sans aucune dépendance. Java pur.

## Packages

| Package | Contenu |
| --- | --- |
| `ddd.interfaces` | `BaseEntity`, `DDDEntity`, `AggregateRoot`, `EntityID`, `DDDRepository`, `DomainEvent`, `DomainEventListener`, `DomainEventPublisher`, `EventDescriptor` |
| `ddd.abstractions` | `AbstractAggregateRootWithEvents`, `DomainEventBase`, `DomainEventMetaData`, `DomainEventsFactory` |
| `ddd.exceptions` | `CodeException` |
| `ddd.exceptions.aggregatException` | `AggregatException` et ses sous-classes sans identifiant |
| `ddd.exceptions.aggregatWithIdException` | `AggregatWithIdException` et ses sous-classes portant l'`aggregateId` |
| `cqrs.interfaces` | `Command`, `CommandHandler` |
| `cqrs.commandResult` | `CommandResult` (scellée), `AggregateRootResult`, `DDDEntityResult` |
| `*.annotations` | `@CommandHandlerComponent`, `@DomainEventListenerComponent`, `@DomainEventPublisherComponent`, `@DomainService`, `@Stub` |
| `hexagone.abstractions` | `RepoInMemory` |

Les annotations sont des marqueurs vides, sans lien avec Spring. C'est `hexalib-infra` qui les
interprète pour enregistrer les beans.

## Identité

L'identifiant d'une **entité autonome** est obligatoirement un value object `EntityID<T>`, jamais un
type brut :

```java
public record CustomerId(UUID value) implements EntityID<UUID> { }
```

Le type brut `T` reste au choix de l'application (`UUID`, `Long`, `String`…) ; c'est son emballage
qui est imposé. Deux points à respecter :

- **`equals`/`hashCode` sur la valeur enveloppée** — les identifiants servent de clé de `HashMap`
  dans `RepoInMemory`. Un `record` les fournit ; une classe ordinaire doit les écrire.
- **l'accesseur s'appelle `value()`**, pas `id()`, pour que `customer.id().value()` reste lisible.

## Trois niveaux d'entité

```
BaseEntity<ID>                            identité, rien d'autre. ID non contraint.
│                                         → une entité interne à un agrégat s'arrête ici
└── DDDEntity<ID extends EntityID<?>>     autonome : sa propre unité de cohérence
     │                                    → ce qu'un DDDRepository charge et sauve
     └── AggregateRoot<ID>                DDDEntity + entités filles + événements de domaine
```

C'est le mécanisme d'**opt-in** de la lib : on ne paie que le niveau que l'on déclare.

```java
// référentiel CRUD : un record, aucun héritage, aucun événement
public record Country(CountryId id, String label) implements DDDEntity<CountryId> { }

// vrai agrégat : racine, entités filles, événements
public class Customer extends AbstractAggregateRootWithEvents<CustomerId> { … }

// entité interne : identité locale, inéligible au repository
public record OrderLine(Integer id, Sku sku) implements BaseEntity<Integer> { }
```

`DDDRepository<E extends DDDEntity<ID>, ID extends EntityID<?>>` se borne sur le niveau autonome :
donner un repository à une `BaseEntity` est une erreur de compilation, ce qui matérialise la
frontière d'agrégat dans le système de types.

Une réserve de vocabulaire, assumée : au sens strict du DDD, `OrderLine` **est** une entité, et elle
n'est pourtant pas une `DDDEntity`. Le nom retient l'appariement avec `DDDRepository`, qui sert le
chemin fréquent ; le sens large est porté par `BaseEntity`.

## Agrégat

```java
public class Customer extends AbstractAggregateRootWithEvents<CustomerId> {

    private final CustomerId id;
    private String email;

    @Override
    public CustomerId id() {
        return id;
    }

    public void changeEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new AggregatIllegalArgumentException(Customer.class, "email vide");
        }
        this.email = newEmail;
        DomainEventsFactory.enqueue(
                        (Customer c, String e) -> new EmailChanged(DomainEventMetaData.of(c), e))
                .withPayload(newEmail)
                .on(this);
    }
}
```

## Événement

```java
public record EmailChanged(
        DomainEventMetaData<Customer, CustomerId> metaData,
        Object payload
) implements DomainEventBase<Customer, CustomerId> {

    @Override
    public Class<Customer> aggregateClass() {
        return Customer.class;
    }
}
```

`DomainEventBase` fournit `name()` (nom de classe simple), `aggregateID()` et `occuredOn()`.

## Handler

```java
@CommandHandlerComponent
public class ChangeEmailHandler implements CommandHandler<ChangeEmail> {

    private final CustomerRepository repository;

    @Override
    public Class<ChangeEmail> listenToCommand() {
        return ChangeEmail.class;
    }

    @Override
    public CommandResult<?> handle(ChangeEmail command) {
        var customer = repository.getOrThrow(command.customerId());
        customer.changeEmail(command.newEmail());
        repository.save(customer);
        return AggregateRootResult.of(customer);
    }
}
```

Le suffixe `Component` des annotations évite la collision de nom avec les interfaces qu'elles
marquent (`CommandHandler`, `DomainEventListener`, `DomainEventPublisher`).

## Résultat de commande

`CommandResult` est **scellée** : `permits AggregateRootResult, DDDEntityResult`. L'axe de variation
est la présence d'événements, pas le succès — le domaine n'a pas de variante d'échec, les erreurs
voyagent en exception jusqu'à `UnitOfWorkMiddleware`.

```java
return AggregateRootResult.of(customer);   // événements relus sur la racine
return DDDEntityResult.of(country);        // référentiel : aucun événement
```

Une seule donnée est transmise dans les deux cas : l'entité. Les événements non publiés restent
**portés par l'agrégat** — `AggregateRootResult.domainEvents()` les relit à chaque appel, sans en
conserver de copie, et `DDDEntityResult.domainEvents()` rend une liste vide par construction.

Le scellement garantit qu'aucune troisième implémentation ne contournera ce contrat. Il permet
aussi le `switch` exhaustif, sans `default` :

```java
var kind = switch (result) {
    case AggregateRootResult<?> a -> …;
    case DDDEntityResult<?> e -> …;
};
```

Java ne sait pas exprimer « `DDDEntity` mais pas `AggregateRoot` » : rien n'empêche donc
d'emballer une racine dans un `DDDEntityResult`, ce qui perdrait ses événements. Le constructeur
canonique de `DDDEntityResult` **rejette ce cas** avec un message explicite, plutôt que de le laisser
échouer en silence.

## Repository

```java
public interface DDDRepository<E extends DDDEntity<ID>, ID extends EntityID<?>> {

    Class<E> aggregateClass();

    Optional<E> find(ID id);

    default E getOrThrow(ID id) { ... }   // AggregatNotFoundException si absent

    E save(E entity);
}
```

`find` n'accepte que le value object d'identité : passer un `UUID` nu est une erreur de compilation,
et deux agrégats dont l'identifiant repose sur le même type brut ne sont plus interchangeables.

`find` ne lève rien : l'absence est un cas normal, rendu par un `Optional` vide. Quand le handler
exige l'agrégat, `getOrThrow` fait la levée à sa place — il construit l'`AggregatNotFoundException`
à partir de `aggregateClass()` et de l'`id`, d'où la présence de cet accesseur au contrat.

## Exceptions

Chaque exception métier porte un `CodeException` qui sera traduit en statut par la couche infra.

Deux branches selon ce que l'exception sait de l'agrégat fautif :

- `AggregatException(Class<?> aggregateClass, CodeException, String message)` — la classe seule ;
- `AggregatWithIdException(Class<?> aggregateClass, EntityID<?> aggregateId, CodeException, String message)`
  — la classe **et** l'identifiant de l'instance, exposé par `aggregateId()`. Le paramètre étant typé
  `EntityID<?>`, un identifiant brut ne peut pas s'y glisser ; les messages exploitent `value()` et
  affichent donc `id inconnu : 3f2a…` plutôt que `CustomerId[value=3f2a…]`.

| Exception | Branche | Code |
| --- | --- | --- |
| `AggregatIllegalArgumentException` | `AggregatException` | `BAD_REQUEST_400` |
| `AggregatUnauthorizedException` | `AggregatException` | `UNAUTHORIZED_401` |
| `AggregatStateException` | `AggregatException` | `FORBIDDEN_403` |
| `AggregatNotFoundException` | `AggregatWithIdException` | `NOT_FOUND_404` |
| `AggregatConcurrentModificationException` | `AggregatWithIdException` | `CONFLICT_409` |

## Test

`RepoInMemory<E, ID>` fournit une implémentation `HashMap` de `DDDRepository` pour les stubs :

```java
@Stub
public class CustomerRepoInMemory extends RepoInMemory<Customer, CustomerId>
        implements CustomerRepository {

    public CustomerRepoInMemory() {
        super(Customer.class);
    }
}
```

La classe d'agrégat passée au constructeur alimente `aggregateClass()`, donc les messages de
`getOrThrow`. `find` retourne le contenu de la `HashMap` en `Optional`, et `findAll()` s'y ajoute
pour les besoins de test. La `HashMap` étant indexée par le value object, c'est ici que se paie un
`EntityID` sans `equals`/`hashCode` : les lectures échouent silencieusement.
