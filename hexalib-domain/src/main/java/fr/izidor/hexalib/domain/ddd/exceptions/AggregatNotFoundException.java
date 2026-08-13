package fr.izidor.hexalib.domain.ddd.exceptions;

public class AggregatNotFoundException extends AggregatException {

    public AggregatNotFoundException(Class<?> aggregateClass, String message) {
        super(aggregateClass, CodeException.NOT_FOUND_404, message);
    }

}
