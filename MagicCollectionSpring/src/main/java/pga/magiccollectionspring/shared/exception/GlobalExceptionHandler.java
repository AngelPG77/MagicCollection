package pga.magiccollectionspring.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ex.getMessage(), ErrorCode.RESOURCE_NOT_FOUND, 404));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex) {
        ErrorCode code = ex.getMessage().toLowerCase().contains("username") 
                ? ErrorCode.USERNAME_ALREADY_EXISTS 
                : ErrorCode.RESOURCE_CONFLICT;
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(ex.getMessage(), code, 409));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        ErrorCode code = ex.getMessage().toLowerCase().contains("contraseña") 
                ? ErrorCode.CURRENT_PASSWORD_INCORRECT 
                : ErrorCode.SESSION_NOT_FOUND;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(ex.getMessage(), code, 401));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of("Usuario o contraseña incorrectos", ErrorCode.CREDENTIALS_INVALID, 401));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthServiceException(InternalAuthenticationServiceException ex) {
        if (ex.getCause() instanceof ResourceNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorResponse.of(ex.getCause().getMessage(), ErrorCode.USER_NOT_FOUND, 404));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of("Error de autenticación", ErrorCode.CREDENTIALS_INVALID, 401));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternal(ExternalServiceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.of(ex.getMessage(), ErrorCode.EXTERNAL_SERVICE_ERROR, 502));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(ex.getMessage(), ErrorCode.VALIDATION_ERROR, 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("Error interno del servidor. Por favor, inténtalo más tarde.", 
                        ErrorCode.INTERNAL_ERROR, 500));
    }
}