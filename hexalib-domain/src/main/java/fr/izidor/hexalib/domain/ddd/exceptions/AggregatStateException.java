package fr.izidor.hexalib.domain.ddd.exceptions;

public class AggregatStateException extends AggregatException {

    public AggregatStateException(Class<?> aggregateClass, String message) {
        super(aggregateClass, CodeException.FORBIDDEN_403, message);
    }

}
