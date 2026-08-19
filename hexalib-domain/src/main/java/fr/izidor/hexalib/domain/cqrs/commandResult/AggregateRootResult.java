package fr.izidor.hexalib.domain.cqrs.commandResult;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Résultat de l'exécution d'une commande portant sur une racine d'agrégat
 */
public record AggregateRootResult<A extends AggregateRoot<?>>(
        LocalDateTime executedOn,
        A entity,
        List<DomainEvent> uncommittedEvents
) implements CommandResult<A> {


    public static <A extends AggregateRoot<?>> AggregateRootResult<?> of(A aggregateRoot) {
        return new AggregateRootResult<>(LocalDateTime.now(), aggregateRoot, aggregateRoot.uncommittedEvents());
    }

}
