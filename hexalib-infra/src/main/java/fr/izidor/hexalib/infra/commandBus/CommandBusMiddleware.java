package fr.izidor.hexalib.infra.commandBus;


import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;

public interface CommandBusMiddleware {

    CommandBusMiddleware next();

    ExecutionResult handle(ApplicationCommand appCommand);

}
