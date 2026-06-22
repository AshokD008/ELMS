package com.lms.usermanagementservice.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {

        super(message);
    }
}