package fr.izidor.hexalib.domain.hexagone.abstractions;


import fr.izidor.hexalib.domain.ddd.exceptions.AggregatNotFoundException;
import fr.izidor.hexalib.domain.ddd.exceptions.AggregatStateException;
import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DDDRepository;

import java.util.Collection;
import java.util.HashMap;

public abstract class RepoInMemory<A extends AggregateRoot<ID>, ID>
        implements DDDRepository<A,ID> {

    private final Class<A> aggregateClass;
    private final HashMap<ID, A> repoInMemory = new HashMap<>();

    protected RepoInMemory(Class<A> aggregateClass) {
        this.aggregateClass = aggregateClass;
    }

    public Collection<A> findAll() {
        return this.repoInMemory.values();
    }


    public A find(ID id) {
        if (!this.repoInMemory.containsKey(id)) {
            throw new AggregatNotFoundException(aggregateClass, "id : " + id);
        }
        return repoInMemory.get(id);
    }


    public A save(A toSave) {
        if (toSave == null) {
            throw new AggregatStateException(aggregateClass, "Entity cannot be null");
        }
        repoInMemory.put(toSave.id(), toSave);
        return toSave;
    }


}
