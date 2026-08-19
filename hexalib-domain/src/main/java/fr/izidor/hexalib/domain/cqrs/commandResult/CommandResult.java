package fr.izidor.hexalib.domain.cqrs.commandResult;

import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ce que retourne un {@code CommandHandler}. Interface <strong>scellée</strong> : l'axe de
 * variation est la présence d'événements de domaine, pas le succès — le domaine n'a pas de
 * variante d'échec, les erreurs voyagent en exception jusqu'à {@code UnitOfWorkMiddleware}.
 *
 * <p>Le scellement garantit qu'aucune troisième implémentation ne viendra contourner le contrat
 * de {@link #domainEvents()}.
 */
public sealed interface CommandResult<E extends DDDEntity<?>>
        permits AggregateRootResult, DDDEntityResult {

    E aggregate();

    LocalDateTime executedOn();

    /**
     * Les événements non publiés, lus sur l'agrégat à chaque appel — le résultat n'en conserve
     * pas de copie. Vide par construction pour une entité sans racine.
     */
    List<DomainEvent> domainEvents();

}
