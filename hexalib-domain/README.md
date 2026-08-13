# hexalib-domain

Abstractions DDD et CQRS, sans aucune dépendance. Java pur.

## Packages

| Package | Contenu |
| --- | --- |
| `ddd.interfaces` | `AggregateRoot`, `DDDEntity`, `EntityID`, `DDDRepository`, `DomainEvent`, `DomainEventListener`, `DomainEventPublisher`, `EventDescriptor` |
| `ddd.abstractions` | `AbstractAggregateRootWithEvents`, `DomainEventBase`, `DomainEventMetaData`, `DomainEventsFactory` |
| `ddd.exceptions` | `AggregatException` et ses sous-classes, `CodeException` |
| `cqrs.interfaces` | `Command`, `CommandHandler`, `CommandResult` |
| `cqrs.abstractions` | `SuccessCommandResult` |
| `*.annotations` | `@CommandHandlerComponent`, `@DomainEventListenerComponent`, `@DomainEventPublisherComponent`, `@DomainService`, `@Stub` |
| `hexagone.abstractions` | `RepoInMemory` |

Les annotations sont des marqueurs vides, sans lien avec Spring. C'est `hexalib-infra` qui les
interprète pour enregistrer les beans.

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
    public CommandResult handle(ChangeEmail command) {
        var customer = repository.find(command.customerId());
        customer.changeEmail(command.newEmail());
        repository.save(customer);
        return SuccessCommandResult.of(customer, customer.domainEvents());
    }
}
```

Le suffixe `Component` des annotations évite la collision de nom avec les interfaces qu'elles
marquent (`CommandHandler`, `DomainEventListener`, `DomainEventPublisher`).

## Exceptions

Chaque exception métier porte un `CodeException` qui sera traduit en statut par la couche infra.

| Exception | Code |
| --- | --- |
| `AggregatIllegalArgumentException` | `BAD_REQUEST_400` |
| `AggregatUnauthorizedException` | `UNAUTHORIZED_401` |
| `AggregatStateException` | `FORBIDDEN_403` |
| `AggregatNotFoundException` | `NOT_FOUND_404` |

## Test

`RepoInMemory<A, ID>` fournit une implémentation `HashMap` de `DDDRepository` pour les stubs :

```java
@Stub
public class CustomerRepoInMemory extends RepoInMemory<Customer, CustomerId>
        implements CustomerRepository {

    public CustomerRepoInMemory() {
        super(Customer.class);
    }
}
```
