# hexalib-domain

Abstractions DDD et CQRS, sans aucune dépendance. Java pur.

## Packages

| Package | Contenu |
| --- | --- |
| `ddd.interfaces` | `BaseEntity`, `DDDEntity`, `AggregateRoot`, `EntityID`, `DDDRepository`, `DomainEvent`, `DomainEventListener`, `DomainEventPublisher`, `EventDescriptor` |
| `ddd.abstractions` | `AbstractDDDEntity`, `AbstractAggregateRootWithEvents`, `DomainEventBase`, `DomainEventMetaData`, `DomainEventsFactory` |
| `ddd.exceptions` | `HttpStatusCode` |
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

// entité autonome mutable : l'égalité vient du socle, sur l'identité seule
public class Invoice extends AbstractDDDEntity<InvoiceId> { … }

// vrai agrégat : racine, entités filles, événements
public class Customer extends AbstractAggregateRootWithEvents<CustomerId> { … }

// entité interne : identité locale, inéligible au repository
public record OrderLine(Integer id, Sku sku) implements BaseEntity<Integer> { }
```

`DDDRepository<E extends DDDEntity<ID>, ID extends EntityID<?>>` se borne sur le niveau autonome :
donner un repository à une `BaseEntity` est une erreur de compilation, ce qui matérialise la
frontière d'agrégat dans le système de types.

## Égalité

`AbstractDDDEntity` fournit l'égalité attendue d'une entité : `equals`/`hashCode` sur l'`id()` seul,
sous condition de classe strictement identique. Deux instances portant le même identifiant sont la
même entité, quel que soit l'état de leurs attributs. `AbstractAggregateRootWithEvents` en hérite :
une racine d'agrégat n'a pas d'égalité propre, et les événements accumulés n'y entrent pas.

Une entité dont l'`id()` est `null` n'est égale qu'à elle-même — affecter l'`EntityID` à la
construction lève la question.

Un `record` ne pouvant hériter, `Country` ci-dessus porte l'égalité structurelle de **tous** ses
composants, `label` compris : deux libellés différents pour le même `CountryId` y sont deux valeurs
distinctes. Pour l'identité DDD sur une entité mutable, étendre `AbstractDDDEntity`.

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

`AbstractAggregateRootWithEvents` tient la liste des événements **non encore publiés**, exposée par
`uncommittedEvents()` en copie défensive. Le nom décrit un état : ce qui reste à publier, et non
l'historique de l'agrégat. La lecture étant défensive, la mutation passe par `addEvent(...)` et
`resetEvents()`, seuls points d'entrée.

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
return AggregateRootResult.of(customer);   // capture les événements non publiés de la racine
return DDDEntityResult.of(country);        // référentiel : aucun événement
```

L'interface commune ne déclare que `executedOn()` et `entity()`. Le transport des événements n'y
figure pas : seul `AggregateRootResult` porte un composant `uncommittedEvents`, que la fabrique
`of(...)` **lit sur l'agrégat au moment de la construction** — un instantané figé au retour du
handler. `DDDEntityResult`, lui, n'a aucun composant d'événements.

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

C'est ce même `switch` qu'exploite le `Dispatcher` de `hexalib-infra` : la variante du résultat est
son seul signal pour savoir s'il y a des événements à relayer au middleware de publication.

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

Chaque exception métier porte un `HttpStatusCode` qui sera traduit en statut par la couche infra.

Deux branches selon ce que l'exception sait de l'agrégat fautif :

- `AggregatException(Class<?> aggregateClass, CodeException, String message)` — la classe seule ;
- `AggregatWithIdException(Class<?> aggregateClass, EntityID<?> aggregateId, CodeException, String message)`
  — la classe **et** l'identifiant de l'instance, exposé par `aggregateId()`. Le paramètre étant typé
  `EntityID<?>`, un identifiant brut ne peut pas s'y glisser ; les messages exploitent `value()` et
  affichent donc `id inconnu : 3f2a…` plutôt que `CustomerId[value=3f2a…]`.

| Exception | Branche | Code |
| --- | --- | --- |
| `AggregatIllegalArgumentException` | `AggregatException` | `UNPROCESSABLE_ENTITY_422` |
| `AggregatStateException` | `AggregatException` | `UNPROCESSABLE_ENTITY_422` |
| `AggregatUnauthorizedException` | `AggregatException` | `UNAUTHORIZED_401` |
| `AggregatNotFoundException` | `AggregatWithIdException` | `NOT_FOUND_404` |
| `AggregatConcurrentModificationException` | `AggregatWithIdException` | `CONFLICT_409` |

### Message

Le message se compose en trois segments, chacun posé par un niveau :

```
[Customer] NotFoundException : id inconnu : 3f2a…
└────┬───┘ └───────┬────────┘ └──────┬─────────┘
AggregatException  la sous-classe    l'appelant
```

`AggregatException` préfixe la classe d'agrégat entre crochets, chaque sous-classe préfixe son
étiquette sémantique, et le message passé au constructeur ferme la phrase. L'étiquette porte la
nature de l'erreur là où le `HttpStatusCode` ne la donne pas : dans un journal, ou après une
conversion en `INTERNAL_ERROR_500`, elle reste lisible. Une exception ajoutée à la lib suit la même
règle.

### Choix du code

Le `HttpStatusCode` suit la nature de l'erreur, pas la commodité du statut :

- `UNPROCESSABLE_ENTITY_422` couvre la **violation d'invariant du domaine** — argument refusé ou
  transition d'état interdite. La requête est bien formée ; c'est le métier qui la refuse. C'est le
  code du cas courant côté agrégat.
- `BAD_REQUEST_400` appartient à la couche transport — requête malformée, désérialisation, validation
  de surface. Aucune exception du domaine ne l'occupe : il reste disponible pour l'application.
- `UNAUTHORIZED_401` = non **authentifié** ; `FORBIDDEN_403` = authentifié mais non **autorisé**.
  L'enum tranche l'ambiguïté d'usage courant, et `AggregatUnauthorizedException` se range du côté
  de l'authentification.
- `INTERNAL_ERROR_500` n'est levé par aucune exception métier : c'est le code que la couche infra
  attribue à toute `RuntimeException` qui n'est pas une `AggregatException`.

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
