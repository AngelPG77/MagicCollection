package pga.magiccollectionspring.shared.exception;

/**
 * Standardized error codes so the frontend can identify
 * the type of error and display appropriate messages.
 */
public enum ErrorCode {
    // Authentication errors (401)
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    TOKEN_MISSING,
    CREDENTIALS_INVALID,
    SESSION_NOT_FOUND,
    CURRENT_PASSWORD_INCORRECT,

    // Authorization errors (403)
    ACCESS_DENIED,
    INSUFFICIENT_PERMISSIONS,

    // Resource errors (404)
    USER_NOT_FOUND,
    CARD_NOT_FOUND,
    COLLECTION_NOT_FOUND,
    RESOURCE_NOT_FOUND,

    // Conflict errors (409)
    USERNAME_ALREADY_EXISTS,
    EMAIL_ALREADY_EXISTS,
    RESOURCE_CONFLICT,

    // Validation errors (400)
    VALIDATION_ERROR,
    INVALID_REQUEST,

    // Server errors (500, 502)
    INTERNAL_ERROR,
    EXTERNAL_SERVICE_ERROR,

    // Generic error
    UNKNOWN_ERROR
}
