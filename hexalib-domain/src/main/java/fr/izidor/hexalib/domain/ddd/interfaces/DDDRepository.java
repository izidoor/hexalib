package fr.izidor.hexalib.domain.ddd.interfaces;

import fr.izidor.hexalib.domain.ddd.exceptions.NotFoundDomainException;

import java.util.Optional;

public interface DDDRepository<E extends DDDEntity<ID>, ID extends EntityID<?>> {

    Class<E> aggregateClass();

    Optional<E> find(ID id);

    default E getOrThrow(ID id) {
        return find(id).orElseThrow(
                () -> NotFoundDomainException.of(id));
    }

    E save(E entity);


}
