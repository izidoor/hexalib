package fr.izidor.hexalib.domain.ddd.exceptions.aggregatException;


import fr.izidor.hexalib.domain.ddd.exceptions.CodeException;

public abstract class AggregatException extends RuntimeException {

    private final Class<?> aggregateClass;
    public final CodeException codeException;


    public AggregatException(Class<?> aggregateClass, CodeException codeException, String message) {
        super("[" + aggregateClass.getSimpleName() + "] "+ message);
        this.aggregateClass = aggregateClass;
        this.codeException = codeException;
    }

    public AggregatException(Class<?> aggregateClass, CodeException codeException, String message, Throwable cause) {
        super("[" + aggregateClass.getSimpleName() + "] "+ message, cause);
        this.aggregateClass = aggregateClass;
        this.codeException = codeException;
    }
}
