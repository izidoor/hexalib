package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;

import java.time.LocalDateTime;

public record DomainEventMetaData<A extends AggregateRoot<ID>, ID>(
        ID aggregateID,
        LocalDateTime occuredOn
) {

    public static <A extends AggregateRoot<ID>, ID> DomainEventMetaData<A, ID> of(A aggregate) {
        return new DomainEventMetaData<A, ID>(aggregate.id(), LocalDateTime.now());
    }

}
