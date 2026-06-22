package com.lms.usermanagementservice.exception;

public class OTPExpiredException extends RuntimeException {

    public OTPExpiredException(String message) {

        super(message);
    }
}