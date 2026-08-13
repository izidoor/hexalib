package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.EventDescriptor;

public final class DomainEventsFactory {

    public static <A extends AggregateRoot<?>, P> DomainEventBuilder<A, P> enqueue(
            EventDescriptor<A, P> descriptor) {
        return new DomainEventBuilder<>(descriptor);
    }

}
