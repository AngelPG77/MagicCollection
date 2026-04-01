package pga.magiccollectionspring.shared.exception;

import java.time.LocalDateTime;

/**
 * Estructura estándar de respuesta de error para la API.
 * Este formato es interpretado por el frontend para mostrar mensajes amigables.
 */
public record ApiErrorResponse(
        String error,
        String code,
        int status,
        String timestamp
) {
    public ApiErrorResponse(String error, ErrorCode code, int status) {
        this(error, code.name(), status, LocalDateTime.now().toString());
    }

    public static ApiErrorResponse of(String error, ErrorCode code, int status) {
        return new ApiErrorResponse(error, code, status);
    }
}
