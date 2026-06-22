package com.lms.usermanagementservice.validator;

import org.springframework.stereotype.Component;

import com.lms.usermanagementservice.util.ValidationUtil;
import lombok.experimental.UtilityClass;

//@UtilityClass
@Component
public class EmailValidator {

    public static boolean isValid(String email) {

        return ValidationUtil.isValidEmail(email);
    }

    public static void validate(String email) {

        if (!isValid(email)) {

            throw new IllegalArgumentException(
                    "Invalid email address format"
            );
        }
    }
}