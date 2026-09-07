package fr.izidor.hexalib.domain.ddd.exceptions;

import java.util.Map;

import static fr.izidor.hexalib.domain.ddd.exceptions.BaseDomainExceptionContent.NOT_FOUND;

public final class NotFoundDomainException extends DomainException {

    private NotFoundDomainException(DomainExceptionContent content, String detail, Map<String, Object> context) {
        super(content, detail, context);
    }

    public static NotFoundDomainException of(Object id ) {
        return new NotFoundDomainException(NOT_FOUND, String.valueOf(id), Map.of("id", id));
    }
}
