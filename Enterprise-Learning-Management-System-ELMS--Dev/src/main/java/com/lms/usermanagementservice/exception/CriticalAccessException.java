package com.lms.usermanagementservice.exception;

/** Deliberately bypasses ordinary application exception handlers for core RBAC violations. */
public final class CriticalAccessException extends Error {
    public CriticalAccessException(String message) {
        super(message);
    }
}
