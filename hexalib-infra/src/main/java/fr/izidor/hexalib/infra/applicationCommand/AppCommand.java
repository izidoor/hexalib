package fr.izidor.hexalib.infra.applicationCommand;


import fr.izidor.hexalib.infra.applicationResult.ErrorResult;
import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.domain.cqrs.interfaces.Command;
import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppCommand(
        UUID id,
        CommandPhase phase,
        String userId,
        LocalDateTime receivedOn,
        LocalDateTime completedOn,
        String endpoint,
        String commandName,
        Command commandContent,
        Boolean isSuccess,
        CodeException codeException,
        String error
) implements ApplicationCommand {


    public static AppCommand INIT(final Command command, final String userId, final String endpoint) {
        return new AppCommand(
                UUID.randomUUID(),
                CommandPhase.RECEIVED,
                userId,
                LocalDateTime.now(),
                null,
                endpoint,
                command.getClass().getSimpleName(),
                command,
                null,
                null,
                null
        );
    }


    @Override
    public AppCommand withResult(ExecutionResult applicationExecutionResult) {

        final LocalDateTime completedOn = LocalDateTime.now();

        if (applicationExecutionResult.isSuccess()) {
            return new AppCommand(
                    id,
                    CommandPhase.COMPLETED,
                    userId,
                    receivedOn,
                    completedOn,
                    endpoint,
                    commandName,
                    commandContent,
                    true,
                    null,
                    null
            );
        }

        return new AppCommand(
                id,
                CommandPhase.COMPLETED,
                userId,
                receivedOn,
                completedOn,
                endpoint,
                commandName,
                commandContent,
                false,
                ((ErrorResult) applicationExecutionResult).codeException(),
                ((ErrorResult) applicationExecutionResult).error()
        );
    }

}
