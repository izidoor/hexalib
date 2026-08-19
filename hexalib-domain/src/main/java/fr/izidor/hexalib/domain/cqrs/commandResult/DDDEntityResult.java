package fr.izidor.hexalib.domain.cqrs.commandResult;

import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;

import java.time.LocalDateTime;

/**
 * Résultat de l'exécution d'une commande portant sur une entité autonome.
 */
public record DDDEntityResult<E extends DDDEntity<?>>(
        LocalDateTime executedOn,
        E entity
) implements CommandResult<E> {

    /**
     * Java ne sait pas exprimer « {@code DDDEntity} mais pas {@code AggregateRoot} » : une entité racine
     * peut donc être emballée ici. Cette garde transforme l'oubli en échec immédiat.
     */
    public DDDEntityResult {
        if (entity instanceof AggregateRoot<?>) {
            throw new IllegalArgumentException(entity.getClass().getSimpleName()
                    + " est une racine d'agrégat : utiliser AggregateRootResult, "
                    + "sinon les traitements postérieurs pour les aggrégats pourraient être perdus (publication des domain events)");
        }
    }


    public static <E extends DDDEntity<?>> DDDEntityResult<E> of(E entity) {
        return new DDDEntityResult<>(LocalDateTime.now(), entity);
    }

}
