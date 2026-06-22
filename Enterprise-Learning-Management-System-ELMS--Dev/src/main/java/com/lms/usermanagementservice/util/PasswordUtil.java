package com.lms.usermanagementservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER =
            new BCryptPasswordEncoder();

    public static String encodePassword(String password) {

        return PASSWORD_ENCODER.encode(password);
    }

    public static String encode(String password) {
        return encodePassword(password);
    }

    public static boolean matches(
            String rawPassword,
            String encodedPassword
    ) {

        return PASSWORD_ENCODER.matches(
                rawPassword,
                encodedPassword
        );
    }
}
