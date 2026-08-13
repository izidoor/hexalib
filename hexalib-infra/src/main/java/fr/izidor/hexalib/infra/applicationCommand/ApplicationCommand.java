package fr.izidor.hexalib.infra.applicationCommand;


import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.domain.cqrs.interfaces.Command;
import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

import java.time.LocalDateTime;
import java.util.UUID;


public interface ApplicationCommand {

    /**
     * Identifiant de la commande. Partagé par les deux traces qu'elle produit,
     * donc <strong>non unique</strong> dans le journal.
     */
    UUID id();

    CommandPhase phase();

    String userId();

    LocalDateTime receivedOn();

    /** Renseigné uniquement sur une trace {@link CommandPhase#COMPLETED}, {@code null} sinon. */
    LocalDateTime completedOn();

    String endpoint();

    String commandName();

    Command commandContent();

    Boolean isSuccess();

    CodeException codeException();

    String error();

    ApplicationCommand withResult(ExecutionResult executionResult);

}
