package fr.izidor.hexalib.domain.ddd.interfaces;

import java.time.LocalDateTime;

public interface DomainEvent<A extends AggregateRoot<ID>, ID>  {
    Class<A> aggregateClass();
    ID aggregateID();
    String name();
    LocalDateTime occuredOn();
    Object payload();
}
