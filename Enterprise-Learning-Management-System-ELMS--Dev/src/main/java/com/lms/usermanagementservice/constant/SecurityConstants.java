package com.lms.usermanagementservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {

    public static final String AUTH_HEADER =
            "Authorization";

    public static final String TOKEN_PREFIX =
            "Bearer ";

    public static final String ROLE_PREFIX =
            "ROLE_";

    public static final String CLAIM_ROLES =
            "roles";

    public static final String SYSTEM_USER =
            "SYSTEM";

    public static final String ACCESS_DENIED_MESSAGE =
            "Access Denied";

    public static final String INVALID_TOKEN_MESSAGE =
            "Invalid or Expired Token";

    public static final String UNAUTHORIZED_MESSAGE =
            "Unauthorized Access";
}