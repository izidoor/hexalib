package fr.izidor.hexalib.domain.ddd.interfaces;

import java.util.List;

/**
 * Racine d'agrégat : une {@link DDDEntity} qui gouverne des entités filles et accumule les
 * événements de domaine non encore publiés.
 */
public interface AggregateRoot<ID extends EntityID<?>> extends DDDEntity<ID> {

    /**
     * Les événements non encore publiés, en <strong>copie défensive</strong> : la mutation passe
     * par {@link #addEvent(DomainEvent)} et {@link #resetEvents()}.
     */
    List<DomainEvent> uncommittedEvents();

    void addEvent(DomainEvent domainEvent);

    /**
     * Solde les événements non publiés. Aucun middleware ne l'appelle : le moment revient à
     * l'adaptateur de persistance, ou à l'agrégat lui-même.
     */
    void resetEvents();

}
