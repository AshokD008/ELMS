package com.lms.usermanagementservice.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

@UtilityClass
public class OTPUtil {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final int OTP_LENGTH = 6;

    public static String generateOTP() {

        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {

            otp.append(
                    SECURE_RANDOM.nextInt(10)
            );
        }

        return otp.toString();
    }

    public static String generateNumericOtp(int length) {

        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < length; i++) {
            otp.append(SECURE_RANDOM.nextInt(10));
        }

        return otp.toString();
    }

    public static boolean validateOTP(
            String generatedOtp,
            String providedOtp
    ) {

        return generatedOtp != null
                && generatedOtp.equals(providedOtp);
    }
}
