package com.lms.usermanagementservice.validator;

import com.lms.usermanagementservice.util.ValidationUtil;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    public static boolean isValid(String password) {

        return ValidationUtil.isValidPassword(password);
    }

    public static void validate(String password) {

        if (!isValid(password)) {

            throw new IllegalArgumentException(
                    "Password does not meet security requirements"
            );
        }
    }
}
