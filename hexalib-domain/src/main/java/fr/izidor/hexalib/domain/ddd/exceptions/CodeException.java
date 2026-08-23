package fr.izidor.hexalib.domain.ddd.exceptions;

public enum CodeException {
    BAD_REQUEST_400,
    UNAUTHORIZED_401, // = non authentifié
    FORBIDDEN_403, // = non autorisé
    NOT_FOUND_404,
    CONFLICT_409,
    UNPROCESSABLE_ENTITY_422,
    INTERNAL_ERROR_500
}
