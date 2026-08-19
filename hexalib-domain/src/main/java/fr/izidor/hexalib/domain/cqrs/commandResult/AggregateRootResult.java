package fr.izidor.hexalib.domain.cqrs.commandResult;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Résultat portant une racine d'agrégat : ses événements sont relus sur elle à chaque appel.
 */
public record AggregateRootResult<E extends AggregateRoot<?>>(
        E aggregate,
        LocalDateTime executedOn
) implements CommandResult<E> {

    @Override
    public List<DomainEvent> domainEvents() {
        return aggregate.domainEvents();
    }

    public static <E extends AggregateRoot<?>> AggregateRootResult<E> of(E aggregateRoot) {
        return new AggregateRootResult<>(aggregateRoot, LocalDateTime.now());
    }

}
