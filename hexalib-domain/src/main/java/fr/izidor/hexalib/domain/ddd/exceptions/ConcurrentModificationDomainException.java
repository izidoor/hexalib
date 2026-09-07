package fr.izidor.hexalib.domain.ddd.exceptions;

import java.util.Map;

import static fr.izidor.hexalib.domain.ddd.exceptions.BaseDomainExceptionContent.CONCURRENCY;

public final class ConcurrentModificationDomainException extends DomainException {

    private ConcurrentModificationDomainException(DomainExceptionContent content, String detail, Map<String, Object> context) {
        super(content, detail, context);
    }

    public static ConcurrentModificationDomainException of (String detail, Map<String, Object> context) {
        return new ConcurrentModificationDomainException(CONCURRENCY, detail, context);
    }
}
