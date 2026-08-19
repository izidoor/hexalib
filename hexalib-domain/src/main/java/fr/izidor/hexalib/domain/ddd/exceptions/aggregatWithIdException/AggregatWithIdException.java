package fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;
import fr.izidor.hexalib.domain.ddd.exceptions.aggregatException.AggregatException;
import fr.izidor.hexalib.domain.ddd.interfaces.EntityID;

public abstract class AggregatWithIdException extends AggregatException {

    private final EntityID<?> aggregateId;

    protected AggregatWithIdException(Class<?> aggregateClass, EntityID<?> aggregateId,
                                      CodeException codeException, String message) {
        super(aggregateClass, codeException, message);
        this.aggregateId = aggregateId;
    }


    public EntityID<?> aggregateId() { return aggregateId; }
}
