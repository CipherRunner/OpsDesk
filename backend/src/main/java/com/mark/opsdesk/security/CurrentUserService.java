package com.mark.opsdesk.security;

import com.mark.opsdesk.common.exception.ForbiddenException;
import com.mark.opsdesk.common.exception.UnauthorizedException;
import com.mark.opsdesk.user.Role;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CurrentUserService {

	public Optional<AuthenticatedUser> getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof AuthenticatedUser authenticatedUser) {
			return Optional.of(authenticatedUser);
		}

		return authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith("ROLE_"))
				.findFirst()
				.map(authority -> new AuthenticatedUser(
						authentication.getName(),
						Role.valueOf(authority.substring("ROLE_".length()))
				));
	}

	public AuthenticatedUser requireCurrentUser() {
		return getCurrentUser()
				.orElseThrow(() -> new UnauthorizedException("Authentication required"));
	}

	public void requireAnyRole(Role... roles) {
		AuthenticatedUser user = requireCurrentUser();
		boolean allowed = Arrays.stream(roles).anyMatch(role -> role == user.role());
		if (!allowed) {
			throw new ForbiddenException("Access denied");
		}
	}

	public void requireRole(Role role) {
		requireAnyRole(role);
	}
}
