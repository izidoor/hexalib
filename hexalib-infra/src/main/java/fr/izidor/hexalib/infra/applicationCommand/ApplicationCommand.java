package fr.izidor.hexalib.infra.applicationCommand;


import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.domain.cqrs.interfaces.Command;

import java.time.LocalDateTime;
import java.util.UUID;


public interface ApplicationCommand {


    UUID id();

    CommandPhase phase();

    String userId();

    LocalDateTime receivedOn();

    LocalDateTime completedOn();

    String endpoint();

    String commandName();

    Command commandContent();

    Boolean isSuccess();

    RuntimeException exception();

    ApplicationCommand withResult(ExecutionResult executionResult);

}
