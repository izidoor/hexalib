package fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;
import fr.izidor.hexalib.domain.ddd.exceptions.aggregatException.AggregatException;

public abstract class AggregatWIthIdException extends AggregatException {

    private final Object aggregateId;

    protected AggregatWIthIdException(Class<?> aggregateClass, Object aggregateId,
                                      CodeException codeException, String message) {
        super(aggregateClass, codeException, message);
        this.aggregateId = aggregateId;
    }


    public Object aggregateId() { return aggregateId; }
}