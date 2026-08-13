package fr.izidor.hexalib.domain.cqrs.abstractions;

import fr.izidor.hexalib.domain.cqrs.interfaces.CommandResult;
import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;


import java.time.LocalDateTime;
import java.util.List;

public record SuccessCommandResult<A extends AggregateRoot>(
        A aggregate,
        List<DomainEvent> domainEvents,
        LocalDateTime executedOn,
        boolean isSuccess
) implements CommandResult<A> {

    public static <A extends AggregateRoot<ID>, ID> SuccessCommandResult<A> of(
            A aggregate, List<DomainEvent> domainEvents) {
        return new SuccessCommandResult<>(aggregate, domainEvents, LocalDateTime.now(), true);
    }
}
