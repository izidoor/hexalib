package fr.izidor.hexalib.infra.applicationResult;

import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.domain.cqrs.commandResult.CommandResult;
import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SuccessResult<E extends DDDEntity<?>>(
        LocalDateTime executedOn,
        String commandId,
        String userId,
        String aggregateId,
        E aggregate,
        List<DomainEvent> domainEvents
) implements ExecutionResult {

    @Override
    public boolean isSuccess() {
        return true;
    }


    public static SuccessResult of(ApplicationCommand applicationCommand, CommandResult commandResult) {

        return SuccessResult.builder()
                .executedOn(commandResult.executedOn())
                .commandId(applicationCommand.id().toString())
                .userId(applicationCommand.userId())
                .aggregateId(aggregateIdOf(commandResult.aggregate()))
                .aggregate(commandResult.aggregate())
                .domainEvents(commandResult.domainEvents())
                .build();
    }


    /**
     * Rend la valeur brute de l'identifiant, et non le {@code toString()} du value object —
     * le journal des commandes porte {@code 3f2a…} plutôt que {@code CustomerId[value=3f2a…]}.
     */
    private static String aggregateIdOf(DDDEntity<?> aggregate) {
        return String.valueOf(aggregate.id().value());
    }

}
