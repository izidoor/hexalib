package fr.izidor.hexalib.domain.ddd.interfaces;

import java.util.List;

public interface DomainEventPublisher {

    void publishAll(List<DomainEvent> domainEvents);
}
