package fr.izidor.hexalib.domain.ddd.exceptions;

import java.util.Map;

public final class InvariantViolationDomainException extends DomainException {

    private InvariantViolationDomainException(DomainExceptionContent content, String detail, Map<String, Object> context) {
        super(content, detail, context);
    }

    public static InvariantViolationDomainException of (DomainExceptionContent code, String detail, Map<String, Object> context) {
        return new InvariantViolationDomainException(code, detail, context);
    }
}
