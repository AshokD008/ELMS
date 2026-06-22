package com.lms.usermanagementservice.config;

import com.lms.usermanagementservice.security.jwt.JwtAccessDeniedHandler;
import com.lms.usermanagementservice.security.jwt.JwtAuthenticationEntryPoint;
import com.lms.usermanagementservice.security.jwt.JwtAuthenticationFilter;
import com.lms.usermanagementservice.security.userdetails.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // CRUCIAL FOR OPTIONS PREFLIGHT
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // 1. Tells Spring Security to evaluate our dynamic CorsConfig.java mapping
                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // FIX: Explicitly bypass browser options validation checks globally
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public Auth Gateways Mapping
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/register/super-admin",
                                "/api/v1/auth/login",
                                "/api/v1/auth/verify-otp",
                                "/api/v1/auth/verify-login-otp",
                                "/api/v1/auth/resend-otp",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/oauth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()

                        // Role-Based Authorization Access Matrix
                        .requestMatchers("/api/v1/super-admins/**")
                        .hasRole("SUPER_ADMIN")

                        .requestMatchers("/api/v1/college-admins/**")
                        .hasAnyRole("SUPER_ADMIN", "COLLEGE_ADMIN")

                        .requestMatchers("/api/v1/students/**")
                        .hasRole("STUDENT")

                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
