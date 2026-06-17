package com.mark.opsdesk.user.dto;

import com.mark.opsdesk.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank
		@Size(max = 255)
		String username,

		@NotBlank
		@Size(min = 8, max = 255)
		String password,

		@NotNull
		Role role
) {
}
