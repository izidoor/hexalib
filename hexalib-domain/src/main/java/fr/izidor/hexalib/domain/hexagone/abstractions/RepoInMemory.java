package fr.izidor.hexalib.domain.hexagone.abstractions;


import fr.izidor.hexalib.domain.ddd.exceptions.aggregatException.AggregatStateException;
import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DDDRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public abstract class RepoInMemory<A extends AggregateRoot<ID>, ID>
        implements DDDRepository<A,ID> {

    private final Class<A> aggregateClass;
    private final HashMap<ID, A> repoInMemory = new HashMap<>();

    protected RepoInMemory(Class<A> aggregateClass) {
        this.aggregateClass = aggregateClass;
    }

    @Override
    public Class<A> aggregateClass() {
        return aggregateClass;
    }

    public Collection<A> findAll() {
        return this.repoInMemory.values();
    }


    public Optional<A> find(ID id) {
        return Optional.ofNullable(repoInMemory.get(id));
    }


    public A save(A toSave) {
        if (toSave == null) {
            throw new AggregatStateException(aggregateClass, "Entity cannot be null");
        }
        repoInMemory.put(toSave.id(), toSave);
        return toSave;
    }


}
