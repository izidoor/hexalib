package fr.izidor.hexalib.domain.hexagone.abstractions;


import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;
import fr.izidor.hexalib.domain.ddd.interfaces.DDDRepository;
import fr.izidor.hexalib.domain.ddd.interfaces.EntityID;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public abstract class RepoInMemory<E extends DDDEntity<ID>, ID extends EntityID<?>>
        implements DDDRepository<E,ID> {

    private final Class<E> aggregateClass;
    private final HashMap<ID, E> repoInMemory = new HashMap<>();

    protected RepoInMemory(Class<E> aggregateClass) {
        this.aggregateClass = aggregateClass;
    }

    @Override
    public Class<E> aggregateClass() {
        return aggregateClass;
    }

    public Collection<E> findAll() {
        return this.repoInMemory.values();
    }


    public Optional<E> find(ID id) {
        return Optional.ofNullable(repoInMemory.get(id));
    }


    public E save(E toSave) {
        Objects.requireNonNull(toSave,"Entity cannot be null" );
        repoInMemory.put(toSave.id(), toSave);
        return toSave;
    }


}
