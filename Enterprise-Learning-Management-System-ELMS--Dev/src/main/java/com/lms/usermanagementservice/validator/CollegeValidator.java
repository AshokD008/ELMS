package com.lms.usermanagementservice.validator;

import com.lms.usermanagementservice.entity.College;
import org.springframework.stereotype.Component;

@Component
public class CollegeValidator {

    public static boolean isValid(College college) {

        return college != null
                && college.getCollegeName() != null
                && !college.getCollegeName().trim().isEmpty();
    }

    public static void validate(College college) {

        if (!isValid(college)) {

            throw new IllegalArgumentException(
                    "Invalid college details"
            );
        }
    }

    public static void validate(Object request) {
        if (request == null) {
            throw new IllegalArgumentException("Invalid college details");
        }
    }
}
