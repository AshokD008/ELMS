package com.lms.usermanagementservice.security.oauth;

import com.lms.usermanagementservice.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.service.OTPService;
import com.lms.usermanagementservice.enums.OTPType;
import com.lms.usermanagementservice.enums.UserStatus;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final OTPService otpService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2UserPrincipal principal =
                (OAuth2UserPrincipal) oauthToken.getPrincipal();

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal.getEmail(),
                        null,
                        principal.getAuthorities()
                );

        var user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new IllegalStateException("OAuth user was not persisted"));
        if (user.getStatus() != UserStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getEmailVerified())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "OAuth account is pending required approval");
            return;
        }
        otpService.generateAndSendOtp(user, OTPType.LOGIN_VERIFICATION);

        log.info("OAuth2 login success for user: {}",
                principal.getEmail());

        response.setContentType("application/json");

        response.getWriter().write("""
                {
                    "message": "Login OTP sent to your verified email"
                }
                """);
    }
}
