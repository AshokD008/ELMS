package com.lms.usermanagementservice.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtil {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private static final String MOBILE_REGEX =
            "^[6-9][0-9]{9}$";

    private static final String PASSWORD_REGEX =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,20}$";

    public static boolean isValidEmail(String email) {

        return email != null
                && Pattern.matches(EMAIL_REGEX, email);
    }

    public static boolean isValidMobile(String mobile) {

        return mobile != null
                && Pattern.matches(MOBILE_REGEX, mobile);
    }

    public static boolean isValidPassword(String password) {

        return password != null
                && Pattern.matches(PASSWORD_REGEX, password);
    }

    public static boolean isNullOrEmpty(String value) {

        return value == null || value.trim().isEmpty();
    }
}