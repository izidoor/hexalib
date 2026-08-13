package fr.izidor.hexalib.domain.cqrs.interfaces;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface CommandResult<A extends AggregateRoot> {

    LocalDateTime executedOn();

    A aggregate();

    List<DomainEvent> domainEvents();

    boolean isSuccess();

}
