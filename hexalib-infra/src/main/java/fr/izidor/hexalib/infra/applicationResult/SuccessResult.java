package fr.izidor.hexalib.infra.applicationResult;

import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.domain.cqrs.interfaces.CommandResult;
import fr.izidor.hexalib.domain.ddd.interfaces.AggregateRoot;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SuccessResult<A extends AggregateRoot<ID>, ID>(
        LocalDateTime executedOn,
        String commandId,
        String userId,
        String aggregateId,
        A aggregate,
        List<DomainEvent<A, ID>> domainEvents
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
                .aggregateId(commandResult.aggregate().id().toString())
                .aggregate(commandResult.aggregate())
                .domainEvents(commandResult.domainEvents())
                .build();
    }

}
