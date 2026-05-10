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
 * Custom handler for Spring Security authentication errors.
 * Converts default 401/403 responses into structured JSON responses.
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
                    message = "The session has expired. Please log in again.";
                }
                case "invalid" -> {
                    code = ErrorCode.TOKEN_INVALID;
                    message = "Invalid authentication token. Please log in again.";
                }
                default -> {
                    code = ErrorCode.TOKEN_MISSING;
                    message = "No authenticated session found.";
                }
            }
        } else {
            // If there is no jwt_error but there was an Authorization header, the token is invalid
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                code = ErrorCode.TOKEN_INVALID;
                message = "Invalid authentication token. Please log in again.";
            } else {
                code = ErrorCode.TOKEN_MISSING;
                message = "Authentication is required to access this resource.";
            }
        }

        ApiErrorResponse errorResponse = ApiErrorResponse.of(message, code, HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
