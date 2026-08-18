package fr.izidor.hexalib.domain.ddd.exceptions.aggregatException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

public class AggregatStateException extends AggregatException {

    public AggregatStateException(Class<?> aggregateClass, String message) {
        super(aggregateClass, CodeException.FORBIDDEN_403, message);
    }

}
