package com.lms.usermanagementservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {

    public static final String APPLICATION_NAME =
            "LMS Telugu User Management Service";

    public static final String DEFAULT_TIMEZONE =
            "Asia/Kolkata";

    public static final String DEFAULT_LANGUAGE =
            "en";

    public static final Integer OTP_EXPIRATION_MINUTES =
            5;

    public static final Integer MAX_LOGIN_ATTEMPTS =
            5;

    public static final Integer PASSWORD_MIN_LENGTH =
            8;

    public static final Integer PASSWORD_MAX_LENGTH =
            20;

    public static final Integer DEFAULT_PAGE_NUMBER =
            0;

    public static final Integer DEFAULT_PAGE_SIZE =
            10;

    public static final String DEFAULT_SORT_BY =
            "createdAt";

    public static final String DEFAULT_SORT_DIRECTION =
            "DESC";
}