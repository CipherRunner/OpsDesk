package com.mark.opsdesk.user.dto;

import com.mark.opsdesk.user.Role;

public record CurrentUserResponse(
		Long id,
		String username,
		Role role
) {
}
