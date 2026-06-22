package com.lms.usermanagementservice.validator;

import com.lms.usermanagementservice.entity.Role;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RoleValidator {

    public static boolean isValid(Role role) {

        return role != null
                && role.getRoleName() != null
                && !role.getRoleName().trim().isEmpty();
    }

    public static void validate(Role role) {

        if (!isValid(role)) {

            throw new IllegalArgumentException(
                    "Invalid role details"
            );
        }
    }
}