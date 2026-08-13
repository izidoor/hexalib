package fr.izidor.hexalib.domain.ddd.interfaces;

@FunctionalInterface
public interface EventDescriptor<A extends AggregateRoot<?>, P > {

    DomainEvent<A, ?> apply(A aggregateRoot, P eventPayload);


}
