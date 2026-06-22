package com.lms.usermanagementservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class APIConstants {

    public static final String API_VERSION =
            "/api/v1";

    public static final String AUTH_BASE_URL =
            API_VERSION + "/auth";

    public static final String STUDENT_BASE_URL =
            API_VERSION + "/students";

    public static final String COLLEGE_BASE_URL =
            API_VERSION + "/colleges";

    public static final String ROLE_BASE_URL =
            API_VERSION + "/roles";

    public static final String PERMISSION_BASE_URL =
            API_VERSION + "/permissions";

    public static final String PROFILE_BASE_URL =
            API_VERSION + "/profiles";

    public static final String DEVICE_BASE_URL =
            API_VERSION + "/devices";

    public static final String SESSION_BASE_URL =
            API_VERSION + "/sessions";

    public static final String OAUTH_BASE_URL =
            API_VERSION + "/oauth";

    public static final String VERIFICATION_BASE_URL =
            API_VERSION + "/verifications";
}