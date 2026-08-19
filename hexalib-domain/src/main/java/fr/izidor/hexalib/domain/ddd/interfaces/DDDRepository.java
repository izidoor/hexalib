package fr.izidor.hexalib.domain.ddd.interfaces;

import fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException.AggregatNotFoundException;

import java.util.Optional;

public interface DDDRepository<E extends DDDEntity<ID>, ID extends EntityID<?>> {

    Class<E> aggregateClass();

    Optional<E> find(ID id);

    default E getOrThrow(ID id) {
        return find(id).orElseThrow(() -> new AggregatNotFoundException(aggregateClass(), id,
                "id inconnu : " + id.value()));
    }

    E save(E entity);


}
