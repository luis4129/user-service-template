package com.luis4129.template.user_service_template.user.dto;

import com.luis4129.template.user_service_template.user.UserStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
