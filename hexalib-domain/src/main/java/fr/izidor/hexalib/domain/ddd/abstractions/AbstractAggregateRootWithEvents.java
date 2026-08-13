package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public abstract class AbstractAggregateRootWithEvents<ID> implements AggregateRoot<ID> {

    protected List<DomainEvent> domainEvents = new ArrayList<>();


    @Override
    public List<DomainEvent> domainEvents() {
        return new ArrayList<>(this.domainEvents);
    }

    @Override
    public void addEvent(DomainEvent domainEvent) {
        AggregateRoot.super.addEvent(domainEvent);
    }

    @Override
    public void resetEvents() {
        AggregateRoot.super.resetEvents();
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
