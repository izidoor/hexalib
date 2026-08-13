package fr.izidor.hexalib.infra.commandBus.middleware;


import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.infra.applicationResult.ExecutionResult;
import fr.izidor.hexalib.infra.applicationResult.SuccessResult;
import fr.izidor.hexalib.infra.commandBus.CommandBusMiddleware;
import fr.izidor.hexalib.domain.cqrs.interfaces.CommandHandler;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class Dispatcher implements CommandBusMiddleware {

    @Override
    public CommandBusMiddleware next() {
        return null;
    }


    private final List<CommandHandler> commandHandlers;


    @Override
    public ExecutionResult handle(ApplicationCommand appCommand) {

        var handler = commandHandlers.stream()
                .filter(isHandlerFor(appCommand.commandContent().getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No command executor found for command " + appCommand.commandContent().getClass()));

        var commandResult = handler.handle(appCommand.commandContent());

        return SuccessResult.of(appCommand,  commandResult);
    }


    Predicate<CommandHandler> isHandlerFor(Class commandClass) {
        return executor -> executor.listenToCommand().equals(commandClass);
    }
}
