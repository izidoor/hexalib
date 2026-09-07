package fr.izidor.hexalib.domain.ddd.exceptions;

public enum BaseDomainExceptionContent implements DomainExceptionContent {
    NOT_FOUND("entity.notFound", HttpStatusCode.NOT_FOUND_404, "Ressource inconnue"),
    CONCURRENCY("entity.concurrent-modification", HttpStatusCode.CONFLICT_409,"Modification concurrente intervenue. Rechargez et réessayer");

    private final String key;
    private final HttpStatusCode httpStatusCode;
    private final String message;

    BaseDomainExceptionContent(String key, HttpStatusCode httpStatusCode, String message) {
        this.key = key;
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public HttpStatusCode httpStatusCode() {
        return httpStatusCode;
    }

    @Override
    public String message() {
        return message;
    }
}
