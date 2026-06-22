package com.lms.usermanagementservice.security.jwt;

import com.lms.usermanagementservice.security.userdetails.CustomUserDetailsService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String token = getJwtFromRequest(request);

            if (StringUtils.isNotBlank(token)) {

                System.out.println("TOKEN: " + token);

                boolean valid =
                        jwtTokenProvider.validateToken(token);

                System.out.println("TOKEN VALID: " + valid);

                if (valid) {

                    String email =
                            jwtTokenProvider.getUsernameFromToken(token);

                    System.out.println("EMAIL: " + email);

                    UserDetails userDetails =
                            customUserDetailsService
                                    .loadUserByUsername(email);

                    System.out.println(
                            "AUTHORITIES: "
                                    + userDetails.getAuthorities()
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    System.out.println("AUTH SUCCESS");
                }
            }
        } catch (Exception ex) {

            ex.printStackTrace();

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.isNotBlank(bearerToken)
                && bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7);
        }

        return null;
    }
}