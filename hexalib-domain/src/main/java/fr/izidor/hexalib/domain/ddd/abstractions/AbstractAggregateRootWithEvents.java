package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;
import fr.izidor.hexalib.domain.ddd.interfaces.EntityID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public abstract class AbstractAggregateRootWithEvents<ID extends EntityID<?>> implements AggregateRoot<ID> {

    protected List<DomainEvent> uncommittedEvents = new ArrayList<>();


    @Override
    public List<DomainEvent> uncommittedEvents() {
        return new ArrayList<>(this.uncommittedEvents);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAggregateRootWithEvents<?> that = (AbstractAggregateRootWithEvents<?>) o;
        return Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id());
    }
}
