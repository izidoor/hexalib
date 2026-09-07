package fr.izidor.hexalib.infra.applicationCommand;


import fr.izidor.hexalib.infra.applicationResult.*;
import fr.izidor.hexalib.domain.cqrs.interfaces.Command;

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
        RuntimeException exception
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
                null
        );
    }




    @Override
    public AppCommand withResult(ExecutionResult applicationExecutionResult) {

        final LocalDateTime completedOn = LocalDateTime.now();


        switch (applicationExecutionResult) {

            case SuccessResult successResult -> {
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
                        null
                );
            }


            case DomainExceptionResult domainExceptionResult -> {
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
                        domainExceptionResult.exception()
                );
            }

            case ConflictResult cpnflictResult -> {
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
                        cpnflictResult.exception()
                );
            }


            case ErrorResult errorResult -> {
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
                        errorResult.exception()
                );
            }

        }
    }

}
