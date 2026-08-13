package fr.izidor.hexalib.infra.commandBus;


import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.infra.applicationCommand.AppCommandRepository;
import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.infra.commandBus.middleware.Dispatcher;
import fr.izidor.hexalib.infra.commandBus.middleware.DomainEventPublisherMiddleware;
import fr.izidor.hexalib.infra.commandBus.middleware.LoggingMiddleware;
import fr.izidor.hexalib.infra.commandBus.middleware.UnitOfWorkMiddleware;
import fr.izidor.hexalib.domain.cqrs.interfaces.CommandHandler;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEventListener;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Component
@Getter
@Accessors(fluent = true)
@Slf4j
public class CommandBus implements CommandBusMiddleware {

    private final AppCommandRepository appCommandRepository;
    private final PlatformTransactionManager transactionManager;
    private final List<CommandHandler> commandHandlers;
    private final CommandBusMiddleware next;

    public CommandBus(
            AppCommandRepository appCommandRepository,
            PlatformTransactionManager transactionManager,
            List<DomainEventListener> domainEventListeners,
            List<CommandHandler> commandHandlers )
    {
        this.appCommandRepository = appCommandRepository;
        this.transactionManager = transactionManager;
        this.commandHandlers = commandHandlers;

        this.next = new LoggingMiddleware(appCommandRepository,
                               new DomainEventPublisherMiddleware(domainEventListeners,
                                    new UnitOfWorkMiddleware(transactionManager,
                                            new Dispatcher(commandHandlers))));
    }


    @Override
    public ExecutionResult handle(ApplicationCommand appCommand) {
        return next.handle(appCommand);
    }
}
