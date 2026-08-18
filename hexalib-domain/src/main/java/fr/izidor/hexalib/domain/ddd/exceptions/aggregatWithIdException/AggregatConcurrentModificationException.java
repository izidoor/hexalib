package fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

public class AggregatConcurrentModificationException extends AggregatWIthIdException {

    public AggregatConcurrentModificationException(Class<?> aggregateClass, Object aggregateId, String message) {
        super(aggregateClass, aggregateId, CodeException.CONFLICT_409, message);
    }
}
