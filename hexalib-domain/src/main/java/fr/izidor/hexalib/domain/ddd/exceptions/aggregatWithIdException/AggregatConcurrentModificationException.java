package fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;
import fr.izidor.hexalib.domain.ddd.interfaces.EntityID;

public class AggregatConcurrentModificationException extends AggregatWithIdException {

    public AggregatConcurrentModificationException(Class<?> aggregateClass, EntityID<?> aggregateId, String message) {
        super(aggregateClass, aggregateId, CodeException.CONFLICT_409,
                "ConcurrentModificationException : " + message);
    }
}
