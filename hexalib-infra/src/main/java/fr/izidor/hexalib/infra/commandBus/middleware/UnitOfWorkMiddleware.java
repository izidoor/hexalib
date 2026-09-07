package fr.izidor.hexalib.infra.commandBus.middleware;

import fr.izidor.hexalib.domain.ddd.exceptions.DomainException;
import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.infra.applicationResult.ConflictResult;
import fr.izidor.hexalib.infra.applicationResult.DomainExceptionResult;
import fr.izidor.hexalib.infra.applicationResult.ErrorResult;
import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.infra.commandBus.CommandBusMiddleware;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
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

        var transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            return transactionTemplate.execute(status -> next.handle(appCommand));

            // EXCEPTION DU DOMAINE
        } catch (DomainException domainException) {
            log.info("Command rejected : {} (id={})",
                    appCommand.commandContent().getClass().getSimpleName(), appCommand.id(), domainException);
            return DomainExceptionResult.of(appCommand, domainException);

            // EXCEPTIONS SQL AU COMMIT
        } catch (OptimisticLockingFailureException | DataIntegrityViolationException conflictException) {
            log.warn("Conflict on command  : {} (id={})",
                    appCommand.commandContent().getClass().getSimpleName(), appCommand.id(),conflictException);
            return ConflictResult.of(appCommand, conflictException);


            // AUTRES EXCEPTIONS RUNTIME ( NPE, IllegalArgument, IllegalState, ...)
        } catch (RuntimeException internalException) {
            log.error("Command failed : {} (id={})",
                    appCommand.commandContent().getClass().getSimpleName(), appCommand.id(), internalException);
            return ErrorResult.of(appCommand, internalException);
        }
    }
}

