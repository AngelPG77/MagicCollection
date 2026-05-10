package pga.magiccollectionspring.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import pga.magiccollectionspring.shared.exception.ApiErrorResponse;
import pga.magiccollectionspring.shared.exception.ErrorCode;

import java.io.IOException;

/**
 * Custom handler for access denied errors (403).
 * Converts default 403 responses into structured JSON responses.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                "You do not have permissions to perform this action.",
                ErrorCode.ACCESS_DENIED,
                HttpServletResponse.SC_FORBIDDEN
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
