package com.luis4129.template.user_service_template.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload for creating a user profile.
 *
 * <p>{@code id} is optional: if the caller (e.g. an authentication-service that
 * already minted the canonical user identity) provides one, it is used as-is;
 * otherwise a new UUID is generated.
 */
public record CreateUserRequest(
        UUID id,

        @NotBlank(message = "{user.validation.email.notBlank}")
        @Email(message = "{user.validation.email.invalid}")
        @Size(max = 320, message = "{user.validation.email.size}")
        String email,

        @NotBlank(message = "{user.validation.firstName.notBlank}")
        @Size(max = 100, message = "{user.validation.firstName.size}")
        String firstName,

        @NotBlank(message = "{user.validation.lastName.notBlank}")
        @Size(max = 100, message = "{user.validation.lastName.size}")
        String lastName,

        @Size(max = 30, message = "{user.validation.phoneNumber.size}")
        String phoneNumber
) {
}
