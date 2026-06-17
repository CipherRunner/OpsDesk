package com.mark.opsdesk.security;

import com.mark.opsdesk.user.Role;

public record AuthenticatedUser(
		String username,
		Role role
) {
}
