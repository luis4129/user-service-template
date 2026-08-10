package com.luis4129.template.user_service_template.common.exception;

import lombok.Getter;

import java.util.UUID;

/**
 * Carries the offending {@code id} rather than a pre-rendered message, so
 * {@link GlobalExceptionHandler} can resolve a localized message for the
 * current request. {@link #getMessage()} stays in plain English - it's for
 * logs/stack traces, not for the client response.
 */
@Getter
public class UserNotFoundException extends RuntimeException {

    private final UUID id;

    public UserNotFoundException(UUID id) {
        super("User not found: " + id);
        this.id = id;
    }
}
