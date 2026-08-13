package fr.izidor.hexalib.domain.ddd.exceptions;


public abstract class AggregatException extends RuntimeException {

    protected Class<?> aggregateClass;
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
