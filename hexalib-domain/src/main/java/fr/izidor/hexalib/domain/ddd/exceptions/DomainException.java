package fr.izidor.hexalib.domain.ddd.exceptions;


import java.util.Map;

public abstract sealed class DomainException extends RuntimeException
    permits NotFoundDomainException, InvariantViolationDomainException, ConcurrentModificationDomainException {

    private final DomainExceptionContent content;
    private final Map<String, Object> context;

    protected DomainException(DomainExceptionContent content, String detail, Map<String, Object> context) {
        super(detail == null ? content.message() : content.message() + " : " + detail);
        this.content = content;
        this.context = context == null ? Map.of() : Map.copyOf(context);
    }

    public DomainExceptionContent content() {return content;}
    public Map<String, Object> context() {return context;}

}
