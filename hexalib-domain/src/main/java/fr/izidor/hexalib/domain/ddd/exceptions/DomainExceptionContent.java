package fr.izidor.hexalib.domain.ddd.exceptions;

public interface DomainExceptionContent {
    String key();
    HttpStatusCode httpStatusCode();
    String message();
}
