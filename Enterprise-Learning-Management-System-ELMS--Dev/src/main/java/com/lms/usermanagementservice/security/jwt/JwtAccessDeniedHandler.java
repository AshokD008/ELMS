package com.lms.usermanagementservice.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.usermanagementservice.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        log.error("Forbidden access error: {}",
                accessDeniedException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpServletResponse.SC_FORBIDDEN)
                .error("Forbidden")
                .message(accessDeniedException.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));
    }
}