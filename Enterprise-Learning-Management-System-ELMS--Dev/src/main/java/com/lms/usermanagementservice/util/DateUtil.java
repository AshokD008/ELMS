package com.lms.usermanagementservice.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@UtilityClass
public class DateUtil {

    private static final String DEFAULT_DATE_FORMAT =
            "yyyy-MM-dd";

    private static final String DEFAULT_DATE_TIME_FORMAT =
            "yyyy-MM-dd HH:mm:ss";

    public static String formatLocalDate(LocalDate localDate) {

        return localDate.format(
                DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)
        );
    }

    public static String formatLocalDateTime(
            LocalDateTime localDateTime
    ) {

        return localDateTime.format(
                DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)
        );
    }

    public static LocalDate parseLocalDate(String date) {

        return LocalDate.parse(
                date,
                DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)
        );
    }

    public static LocalDateTime parseLocalDateTime(
            String dateTime
    ) {

        return LocalDateTime.parse(
                dateTime,
                DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)
        );
    }

    public static Date convertToDate(
            LocalDateTime localDateTime
    ) {

        return Date.from(
                localDateTime.atZone(ZoneId.systemDefault())
                        .toInstant()
        );
    }

    public static LocalDateTime convertToLocalDateTime(
            Date date
    ) {

        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}