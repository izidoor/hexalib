package fr.izidor.hexalib.domain.cqrs.commandResult;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Résultat portant une entité autonome sans événements — le cas d'un référentiel CRUD.
 */
public record DDDEntityResult<E extends DDDEntity<?>>(
        E aggregate,
        LocalDateTime executedOn
) implements CommandResult<E> {

    /**
     * Java ne sait pas exprimer « {@code DDDEntity} mais pas {@code AggregateRoot} » : une racine
     * peut donc être emballée ici, et ses événements seraient perdus en silence. Cette garde
     * transforme l'oubli en échec immédiat.
     */
    public DDDEntityResult {
        if (aggregate instanceof AggregateRoot<?>) {
            throw new IllegalArgumentException(aggregate.getClass().getSimpleName()
                    + " est une racine d'agrégat : utiliser AggregateRootResult, "
                    + "sinon ses événements de domaine sont perdus");
        }
    }

    @Override
    public List<DomainEvent> domainEvents() {
        return List.of();
    }

    public static <E extends DDDEntity<?>> DDDEntityResult<E> of(E entity) {
        return new DDDEntityResult<>(entity, LocalDateTime.now());
    }

}
