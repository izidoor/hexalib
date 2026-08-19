package fr.izidor.hexalib.domain.cqrs.commandResult;

import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ce que retourne un {@code CommandHandler}. Interface <strong>scellée</strong>.
 * Le domaine n'a pas de variante d'échec, les erreurs voyagent en exception jusqu'à {@code UnitOfWorkMiddleware}.
 */
public sealed interface CommandResult<E extends DDDEntity<?>>
        permits AggregateRootResult, DDDEntityResult {


    LocalDateTime executedOn();

    E entity();


}
