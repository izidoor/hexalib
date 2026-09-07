package fr.izidor.hexalib.infra.applicationResult;

import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConflictResult(
        LocalDateTime executedOn,
        String commandId,
        String userId,
        RuntimeException exception

) implements ExecutionResult {

    @Override
    public boolean isSuccess() {
        return false;
    }



    public static ConflictResult of(ApplicationCommand applicationCommand,
                                    RuntimeException exception) {

        return ConflictResult.builder()
                .executedOn(LocalDateTime.now())
                .commandId(applicationCommand.id().toString())
                .userId(applicationCommand.userId())
                .exception(exception)
                .build();
    }

}
