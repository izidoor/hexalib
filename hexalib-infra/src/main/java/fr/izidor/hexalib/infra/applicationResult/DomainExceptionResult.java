package fr.izidor.hexalib.infra.applicationResult;

import fr.izidor.hexalib.domain.ddd.exceptions.DomainException;
import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DomainExceptionResult(
        LocalDateTime executedOn,
        String commandId,
        String userId,
        DomainException exception

) implements ExecutionResult {

    @Override
    public boolean isSuccess() {
        return false;
    }



    public static DomainExceptionResult of(ApplicationCommand applicationCommand,
                                           DomainException exception) {

        return DomainExceptionResult.builder()
                .executedOn(LocalDateTime.now())
                .commandId(applicationCommand.id().toString())
                .userId(applicationCommand.userId())
                .exception(exception)
                .build();
    }

}
