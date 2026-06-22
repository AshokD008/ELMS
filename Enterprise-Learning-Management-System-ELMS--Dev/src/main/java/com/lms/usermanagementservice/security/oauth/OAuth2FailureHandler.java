package com.lms.usermanagementservice.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.usermanagementservice.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        log.error("OAuth2 authentication failed: {}",
                exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
                .error("OAuth2 Authentication Failed")
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));
    }
}