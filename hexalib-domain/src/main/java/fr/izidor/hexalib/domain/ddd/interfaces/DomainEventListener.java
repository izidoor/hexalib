package fr.izidor.hexalib.domain.ddd.interfaces;



public interface DomainEventListener<DE extends DomainEvent> {

    Class<DE> listenToEvent();

    void onEvent(DE event);

}
