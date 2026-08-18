package fr.izidor.hexalib.infra.applicationResult;

import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResult (
        LocalDateTime executedOn,
        String commandId,
        String userId,
        CodeException codeException,
        String error

) implements ExecutionResult {

    @Override
    public boolean isSuccess() {
        return false;
    }



    public static ErrorResult of(ApplicationCommand applicationCommand,
                                          CodeException codeException,
                                          String errorMsg) {

        return ErrorResult.builder()
                .executedOn(LocalDateTime.now())
                .commandId(applicationCommand.id().toString())
                .userId(applicationCommand.userId())
                .codeException(codeException)
                .error(errorMsg)
                .build();
    }

}
