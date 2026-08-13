package fr.izidor.hexalib.domain.ddd.interfaces;

public interface DDDRepository<A extends AggregateRoot<ID>, ID> {

    A find(ID id);

    A save(A aggregateRoot);


}
