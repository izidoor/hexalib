package fr.izidor.hexalib.domain.ddd.interfaces;

import java.util.List;

/**
 * Racine d'agrégat : une {@link DDDEntity} qui gouverne des entités filles et accumule les
 * événements de domaine non encore publiés.
 */
public interface AggregateRoot<ID extends EntityID<?>> extends DDDEntity<ID> {

    /**
     * Les événements non encore publiés, rendus en <strong>copie défensive</strong> : la liste
     * retournée n'est pas le réceptacle des ajouts.
     */
    List<DomainEvent> uncommittedEvents();

    /**
     * Sans état à sa disposition, l'interface ne peut offrir ici d'implémentation par défaut :
     * elle ne pourrait qu'écrire dans la copie rendue par {@link #uncommittedEvents()}, et l'ajout
     * serait perdu en silence. La mutation appartient donc à l'implémentation —
     * {@code AbstractAggregateRootWithEvents} la fournit.
     */
    void addEvent(DomainEvent domainEvent);

    /**
     * Solde les événements non publiés. Aucun middleware ne l'appelle : c'est à l'adaptateur de
     * persistance, ou à l'agrégat lui-même, d'en décider le moment.
     */
    void resetEvents();

}
