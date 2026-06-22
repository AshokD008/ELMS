package com.lms.usermanagementservice.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LogUtil {

    public static void info(String message) {

        log.info(message);
    }

    public static void info(
            String message,
            Object... arguments
    ) {

        log.info(message, arguments);
    }

    public static void warn(String message) {

        log.warn(message);
    }

    public static void warn(
            String message,
            Object... arguments
    ) {

        log.warn(message, arguments);
    }

    public static void error(String message) {

        log.error(message);
    }

    public static void error(
            String message,
            Throwable throwable
    ) {

        log.error(message, throwable);
    }

    public static void debug(String message) {

        log.debug(message);
    }

    public static void debug(
            String message,
            Object... arguments
    ) {

        log.debug(message, arguments);
    }
}