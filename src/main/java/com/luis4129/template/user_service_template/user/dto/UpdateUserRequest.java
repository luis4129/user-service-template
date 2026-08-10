package com.luis4129.template.user_service_template.user.dto;

import com.luis4129.template.user_service_template.user.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Payload for a full (PUT) update of an existing user profile. */
public record UpdateUserRequest(
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
        String phoneNumber,

        @NotNull(message = "{user.validation.status.notNull}")
        UserStatus status
) {
}
