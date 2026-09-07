package fr.izidor.hexalib.infra.commandBus.middleware;

import fr.izidor.hexalib.infra.applicationCommand.ApplicationCommand;
import fr.izidor.hexalib.infra.applicationResult.*;
import fr.izidor.hexalib.infra.commandBus.CommandBusMiddleware;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEvent;
import fr.izidor.hexalib.domain.ddd.interfaces.DomainEventListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Predicate;


@Slf4j
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class DomainEventPublisherMiddleware implements CommandBusMiddleware {

    private final List<DomainEventListener> domainEventListeners;
    private final CommandBusMiddleware next;


    @Override
    public ExecutionResult handle(ApplicationCommand appCommand) {

        var result = next.handle(appCommand);


        return switch (result) {
            case ErrorResult errorResult -> errorResult;
            case ConflictResult conflictResult -> conflictResult;
            case DomainExceptionResult domainExceptionResult -> domainExceptionResult;
            case SuccessResult successResult -> publishEventsAndNext(successResult);
        };
    }


    private ExecutionResult publishEventsAndNext(SuccessResult successResult) {

        var uncommittedEvents = successResult.uncommittedEvents();
        if (uncommittedEvents.isEmpty()) { return successResult; }


        uncommittedEvents.forEach(event -> {
            domainEventListeners.stream()
                    .filter(listenOn(event.getClass()))
                    .forEach(domainEventListener -> {
                        domainEventListener.onEvent((DomainEvent) event);
                        log.info("Domain event {} published for listener {} ", event, domainEventListener.getClass().getSimpleName());
                    });
        });

        return successResult;
    }


    Predicate<DomainEventListener> listenOn(Class eventClass) {
        return listener -> listener.listenToEvent().equals(eventClass);
    }

}
