package fr.izidor.hexalib.infra.applicationResult;

import fr.izidor.hexalib.domain.cqrs.commandResult.AggregateRootResult;
import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.domain.cqrs.commandResult.CommandResult;
import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SuccessResult<E extends DDDEntity<?>>(
        String commandId,
        String userId,
        LocalDateTime executedOn,
        E aggregate,
        List<DomainEvent> uncommittedEvents
) implements ExecutionResult {

    @Override
    public boolean isSuccess() {
        return true;
    }

    /**
     * Fabrique SANS Domain Events.
     * @param applicationCommand
     * @param commandResult
     * @param domainEvents
     * @return
     */
    public static SuccessResult of(ApplicationCommand applicationCommand, CommandResult commandResult) {
        return SuccessResult.builder()
                .commandId(applicationCommand.id().toString())
                .userId(applicationCommand.userId())
                .executedOn(commandResult.executedOn())
                .aggregate(commandResult.entity())
                .uncommittedEvents(List.of())
                .build();
    }


    /**
     * Fabrique avec Domain Events
     * @param applicationCommand
     * @param commandResult
     * @param domainEvents
     * @return
     */
    public static SuccessResult of(ApplicationCommand applicationCommand, CommandResult commandResult, List<DomainEvent> uncommittedEvents) {
        return SuccessResult.builder()
                .commandId(applicationCommand.id().toString())
                .userId(applicationCommand.userId())
                .executedOn(commandResult.executedOn())
                .aggregate(commandResult.entity())
                .uncommittedEvents(uncommittedEvents)
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
