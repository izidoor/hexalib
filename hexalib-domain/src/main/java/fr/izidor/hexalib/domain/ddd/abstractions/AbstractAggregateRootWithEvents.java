package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;
import fr.izidor.hexalib.domain.ddd.interfaces.EntityID;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation de l'accumulation d'événements de domaine d'une {@link AggregateRoot}. L'égalité
 * est celle de toute entité, héritée d'{@link AbstractDDDEntity} : l'{@code id()} seul.
 */
public abstract class AbstractAggregateRootWithEvents<ID extends EntityID<?>>
        extends AbstractDDDEntity<ID> implements AggregateRoot<ID> {

    protected List<DomainEvent> uncommittedEvents = new ArrayList<>();


    @Override
    public List<DomainEvent> uncommittedEvents() {
        return new ArrayList<>(this.uncommittedEvents);
    }

    @Override
    public void addEvent(DomainEvent domainEvent) {
        this.uncommittedEvents.add(domainEvent);
    }

    @Override
    public void resetEvents() {
        this.uncommittedEvents.clear();
    }
}
