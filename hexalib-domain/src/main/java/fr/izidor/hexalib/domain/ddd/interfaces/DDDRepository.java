package fr.izidor.hexalib.domain.ddd.interfaces;

import fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException.AggregatNotFoundException;

import java.util.Optional;

public interface DDDRepository<A extends AggregateRoot<ID>, ID> {

    Class<A> aggregateClass();

    Optional<A> find(ID id);

    default A getOrThrow(ID id) {
        return find(id).orElseThrow(() -> new AggregatNotFoundException(aggregateClass(), id,
                "id inconnu : " + id.toString()));
    }

    A save(A aggregateRoot);


}
