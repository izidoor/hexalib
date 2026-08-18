package fr.izidor.hexalib.domain.ddd.exceptions.aggregatException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

public class AggregatUnauthorizedException extends AggregatException {

    public AggregatUnauthorizedException(Class<?> aggregateClass, String message) {
        super(aggregateClass, CodeException.UNAUTHORIZED_401, message);
    }

    public AggregatUnauthorizedException(Class<?> aggregateClass, String message, Throwable cause) {
        super(aggregateClass, CodeException.UNAUTHORIZED_401, message, cause);
    }


}
