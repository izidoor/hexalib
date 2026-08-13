package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;
import fr.izidor.hexalib.domain.ddd.interfaces.EventDescriptor;

import java.time.LocalDateTime;

public interface DomainEventBase<A extends AggregateRoot<ID>, ID> extends DomainEvent<A ,ID> {

    DomainEventMetaData<A, ID> metaData();

    @Override
    default String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    default ID aggregateID() {
        return metaData().aggregateID();
    }

    @Override
    default LocalDateTime occuredOn() {
        return metaData().occuredOn();
    }


    static <A extends AggregateRoot<?>, P> DomainEventBuilder<A, P> enqueue(
            EventDescriptor<A, P> descriptor) {
        return new DomainEventBuilder<>(descriptor);
    }


}
