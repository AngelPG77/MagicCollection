package pga.magiccollectionspring.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pga.magiccollectionspring.shared.exception.ApiErrorResponse;
import pga.magiccollectionspring.shared.exception.ErrorCode;

import java.io.IOException;

/**
 * Manejador personalizado para errores de autenticación de Spring Security.
 * Convierte las respuestas 401/403 por defecto en respuestas JSON estructuradas.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorAttribute = (String) request.getAttribute("jwt_error");
        ErrorCode code;
        String message;

        if (errorAttribute != null) {
            switch (errorAttribute) {
                case "expired" -> {
                    code = ErrorCode.TOKEN_EXPIRED;
                    message = "La sesión ha expirado. Por favor, inicia sesión nuevamente.";
                }
                case "invalid" -> {
                    code = ErrorCode.TOKEN_INVALID;
                    message = "Token de autenticación inválido. Por favor, inicia sesión nuevamente.";
                }
                default -> {
                    code = ErrorCode.TOKEN_MISSING;
                    message = "No hay una sesión autenticada.";
                }
            }
        } else {
            // Si no hay jwt_error pero sí había header Authorization, el token es inválido
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                code = ErrorCode.TOKEN_INVALID;
                message = "Token de autenticación inválido. Por favor, inicia sesión nuevamente.";
            } else {
                code = ErrorCode.TOKEN_MISSING;
                message = "Se requiere autenticación para acceder a este recurso.";
            }
        }

        ApiErrorResponse errorResponse = ApiErrorResponse.of(message, code, HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
