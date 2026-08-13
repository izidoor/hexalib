package fr.izidor.hexalib.infra.commandBus.middleware;

import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.infra.applicationResult.ErrorResult;
import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.infra.commandBus.CommandBusMiddleware;
import fr.izidor.hexalib.domain.ddd.exceptions.AggregatException;
import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;


@Slf4j
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class UnitOfWorkMiddleware implements CommandBusMiddleware {

    private final PlatformTransactionManager transactionManager;
    private final CommandBusMiddleware next;


    @Override
    public ExecutionResult handle(ApplicationCommand appCommand) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate.execute(status -> {
            try {
                return next.handle(appCommand);
            } catch (AggregatException e) {
                log.error("Error while handling command {} (id={}) in a new transaction", appCommand.commandContent().getClass().getSimpleName(), appCommand.id(), e);
                status.setRollbackOnly();
                return ErrorResult.of(
                        appCommand,
                        e.codeException,
                        e.getMessage()
                );
            } catch (RuntimeException e) {
                log.error("Error while handling command {} (id={}) in a new transaction", appCommand.commandContent().getClass().getSimpleName(), appCommand.id(), e);
                status.setRollbackOnly();
                return ErrorResult.of(
                        appCommand,
                        CodeException.INTERNAL_ERROR_500,
                        e.getMessage()
                );
            }
        });
    }

}
