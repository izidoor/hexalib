package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.EventDescriptor;

public class DomainEventBuilder<A extends AggregateRoot<?>, P>  {

    private final EventDescriptor<A, P> descriptor;
    private P payload;

    DomainEventBuilder(EventDescriptor<A, P> descriptor) {
        this.descriptor = descriptor;
    }

    public DomainEventBuilder<A, P> withPayload(P payload) {
        this.payload = payload;
        return this;
    }

    public void on(A aggregate) {
        var event = descriptor.apply(aggregate, payload);
        aggregate.addEvent(event);
    }
}
