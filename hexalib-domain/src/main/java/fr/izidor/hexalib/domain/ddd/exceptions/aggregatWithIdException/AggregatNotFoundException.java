package fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

public class AggregatNotFoundException extends AggregatWIthIdException {

    public AggregatNotFoundException(Class<?> aggregateClass, Object aggregateId, String message) {
        super(aggregateClass, aggregateId, CodeException.NOT_FOUND_404, message);
    }

}
