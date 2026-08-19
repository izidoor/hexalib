package fr.izidor.hexalib.domain.ddd.exceptions.aggregatWithIdException;

import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;
import fr.izidor.hexalib.domain.ddd.interfaces.EntityID;

public class AggregatNotFoundException extends AggregatWithIdException {

    public AggregatNotFoundException(Class<?> aggregateClass, EntityID<?> aggregateId, String message) {
        super(aggregateClass, aggregateId, CodeException.NOT_FOUND_404, message);
    }

}
