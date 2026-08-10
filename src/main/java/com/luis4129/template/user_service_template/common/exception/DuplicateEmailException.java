package com.luis4129.template.user_service_template.common.exception;

import lombok.Getter;

/**
 * Carries the offending {@code email} rather than a pre-rendered message, so
 * {@link GlobalExceptionHandler} can resolve a localized message for the
 * current request. {@link #getMessage()} stays in plain English - it's for
 * logs/stack traces, not for the client response.
 */
@Getter
public class DuplicateEmailException extends RuntimeException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("A user with email " + email + " already exists");
        this.email = email;
    }
}
