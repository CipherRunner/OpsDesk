package com.mark.opsdesk.user.dto;

import com.mark.opsdesk.user.Role;

import java.time.Instant;

public record UserResponse(
		Long id,
		String username,
		Role role,
		Instant createdAt,
		Instant updatedAt
) {
}
