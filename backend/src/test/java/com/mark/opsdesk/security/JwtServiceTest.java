package com.mark.opsdesk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

	private static final String SECRET = "unit-test-secret-unit-test-secret-unit-test-secret";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void rejectsMissingSecret() {
		assertThatThrownBy(() -> new JwtService(OBJECT_MAPPER, "", Duration.ofHours(1)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("OPS_DESK_JWT_SECRET");
	}

	@Test
	void rejectsSecretShorterThan32Bytes() {
		assertThatThrownBy(() -> new JwtService(OBJECT_MAPPER, "too-short", Duration.ofHours(1)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("too short");
	}

	@Test
	void roundTripsUsernameAndRole() {
		JwtService jwtService = new JwtService(OBJECT_MAPPER, SECRET, Duration.ofHours(1));

		String token = jwtService.createToken(user("alice", Role.AGENT));

		assertThat(jwtService.parseToken(token))
				.contains(new AuthenticatedUser("alice", Role.AGENT));
	}

	@Test
	void rejectsTokenSignedWithDifferentSecret() {
		JwtService issuer = new JwtService(OBJECT_MAPPER, SECRET, Duration.ofHours(1));
		JwtService verifier = new JwtService(OBJECT_MAPPER, SECRET + "-other", Duration.ofHours(1));

		String token = issuer.createToken(user("alice", Role.AGENT));

		assertThat(verifier.parseToken(token)).isEmpty();
	}

	@Test
	void rejectsTamperedPayload() {
		JwtService jwtService = new JwtService(OBJECT_MAPPER, SECRET, Duration.ofHours(1));
		String token = jwtService.createToken(user("alice", Role.REQUESTER));
		String[] parts = token.split("\\.");
		String escalatedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
				("{\"sub\":\"alice\",\"role\":\"ADMIN\",\"iat\":0,\"exp\":" + Long.MAX_VALUE + "}").getBytes());

		String tampered = parts[0] + "." + escalatedPayload + "." + parts[2];

		assertThat(jwtService.parseToken(tampered)).isEmpty();
	}

	@Test
	void rejectsExpiredToken() {
		JwtService jwtService = new JwtService(OBJECT_MAPPER, SECRET, Duration.ofSeconds(-60));

		String token = jwtService.createToken(user("alice", Role.AGENT));

		assertThat(jwtService.parseToken(token)).isEmpty();
	}

	@Test
	void rejectsMalformedToken() {
		JwtService jwtService = new JwtService(OBJECT_MAPPER, SECRET, Duration.ofHours(1));

		assertThat(jwtService.parseToken("not-a-jwt")).isEmpty();
		assertThat(jwtService.parseToken("a.b")).isEmpty();
		assertThat(jwtService.parseToken("a.b.c")).isEmpty();
	}

	private static User user(String username, Role role) {
		User user = mock(User.class);
		when(user.getUsername()).thenReturn(username);
		when(user.getRole()).thenReturn(role);
		return user;
	}
}
