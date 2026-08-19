package fr.izidor.hexalib.domain.ddd.interfaces;

import java.util.List;

/**
 * Racine d'agrégat : une {@link DDDEntity} qui gouverne des entités filles et accumule les
 * événements de domaine non encore publiés.
 */
public interface AggregateRoot<ID extends EntityID<?>> extends DDDEntity<ID> {

    List<DomainEvent> uncommittedEvents();

    default void addEvent(DomainEvent domainEvent) {
        uncommittedEvents().add(domainEvent);
    }

    default void resetEvents() {
        uncommittedEvents().clear();
    }

}
