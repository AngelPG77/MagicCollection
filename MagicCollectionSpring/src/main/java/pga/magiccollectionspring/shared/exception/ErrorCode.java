package pga.magiccollectionspring.shared.exception;

/**
 * Códigos de error estandarizados para que el frontend pueda identificar
 * el tipo de error y mostrar mensajes apropiados.
 */
public enum ErrorCode {
    // Errores de autenticación (401)
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    TOKEN_MISSING,
    CREDENTIALS_INVALID,
    SESSION_NOT_FOUND,
    CURRENT_PASSWORD_INCORRECT,

    // Errores de autorización (403)
    ACCESS_DENIED,
    INSUFFICIENT_PERMISSIONS,

    // Errores de recursos (404)
    USER_NOT_FOUND,
    CARD_NOT_FOUND,
    COLLECTION_NOT_FOUND,
    RESOURCE_NOT_FOUND,

    // Errores de conflicto (409)
    USERNAME_ALREADY_EXISTS,
    EMAIL_ALREADY_EXISTS,
    RESOURCE_CONFLICT,

    // Errores de validación (400)
    VALIDATION_ERROR,
    INVALID_REQUEST,

    // Errores de servidor (500, 502)
    INTERNAL_ERROR,
    EXTERNAL_SERVICE_ERROR,

    // Error genérico
    UNKNOWN_ERROR
}
