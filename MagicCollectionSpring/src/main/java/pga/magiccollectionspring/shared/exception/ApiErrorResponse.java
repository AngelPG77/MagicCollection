package pga.magiccollectionspring.shared.exception;

import java.time.LocalDateTime;

/**
 * Standard error response structure for the API.
 * This format is interpreted by the frontend to display friendly messages.
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
