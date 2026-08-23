package fr.izidor.hexalib.domain.ddd.exceptions.aggregatException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

public class AggregatIllegalArgumentException extends AggregatException {

    public AggregatIllegalArgumentException(Class<?> aggregateClass, String message) {
        super(aggregateClass, CodeException.UNPROCESSABLE_ENTITY_422,
                "IllegalArgumentException : " + message);
    }

}
