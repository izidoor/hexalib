package fr.izidor.hexalib.infra.commandBus.middleware;

import fr.izidor.hexalib.infra.applicationCommand.AppCommandRepository;
import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.infra.commandBus.CommandBusMiddleware;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class LoggingMiddleware implements CommandBusMiddleware {

    private final AppCommandRepository appCommandRepository;
    private final CommandBusMiddleware next;



    @Override
    public ExecutionResult handle(ApplicationCommand appCommand) {

        saveQuietly(appCommand);

        var timeStarted = System.currentTimeMillis();

        var result = next.handle(appCommand);

        var timeEnded = System.currentTimeMillis();
        var elapsed = timeEnded - timeStarted;
        log.info("Execution time of {} (id={}) : {} ms. Result : {}", appCommand.commandContent().getClass().getSimpleName(), appCommand.id(), elapsed, result.isSuccess() ? "success" : "failure");

        saveQuietly(appCommand.withResult(result));

        return result;
    }


    private void saveQuietly(ApplicationCommand appCommand) {
        try {
            appCommandRepository.save(appCommand);
        } catch (RuntimeException e) {
            log.error("Observabilité impossible pour la commande {} (id={}, user={}) - execution poursuivie",
                    appCommand.commandName(), appCommand.id(), appCommand.userId(), e);
        }
    }
}
