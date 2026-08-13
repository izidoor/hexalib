package fr.izidor.hexalib.domain.ddd.interfaces;

import java.util.List;

public interface AggregateRoot<ID> extends DDDEntity<ID> {

    List<DomainEvent> domainEvents();

    default void addEvent(DomainEvent domainEvent) {
        domainEvents().add(domainEvent);
    }

    default void resetEvents() {
        domainEvents().clear();
    }

}
