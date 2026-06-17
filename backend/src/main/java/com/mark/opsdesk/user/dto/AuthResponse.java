package com.mark.opsdesk.user.dto;

public record AuthResponse(
		String token,
		CurrentUserResponse user
) {
}
