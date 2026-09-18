package com.mark.opsdesk.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

	private final ObjectMapper objectMapper;
	private final byte[] secret;
	private final Duration expiration;

	/**
	 * HS256 needs a key of at least 256 bits; shorter secrets make the signature brute-forceable.
	 */
	static final int MIN_SECRET_BYTES = 32;

	public JwtService(
			ObjectMapper objectMapper,
			@Value("${opsdesk.security.jwt.secret:}") String secret,
			@Value("${opsdesk.security.jwt.expiration:PT2H}") Duration expiration
	) {
		this.objectMapper = objectMapper;
		this.secret = requireStrongSecret(secret);
		this.expiration = expiration;
	}

	private static byte[] requireStrongSecret(String secret) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException(
					"JWT secret is not configured. Set OPS_DESK_JWT_SECRET to a random value of at least "
							+ MIN_SECRET_BYTES + " characters, or activate the 'dev' profile for local development.");
		}
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException(
					"JWT secret is too short (" + bytes.length + " bytes). OPS_DESK_JWT_SECRET must be at least "
							+ MIN_SECRET_BYTES + " bytes for HS256.");
		}
		return bytes;
	}

	public String createToken(User user) {
		Instant now = Instant.now();
		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.getUsername());
		payload.put("role", user.getRole().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", now.plus(expiration).getEpochSecond());

		String unsignedToken = encode(header) + "." + encode(payload);
		return unsignedToken + "." + sign(unsignedToken);
	}

	public Optional<AuthenticatedUser> parseToken(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				return Optional.empty();
			}

			String unsignedToken = parts[0] + "." + parts[1];
			if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8),
					parts[2].getBytes(StandardCharsets.UTF_8))) {
				return Optional.empty();
			}

			Map<String, Object> payload = objectMapper.readValue(
					DECODER.decode(parts[1]),
					new TypeReference<>() {
					}
			);

			String username = (String) payload.get("sub");
			String role = (String) payload.get("role");
			Number exp = (Number) payload.get("exp");
			if (username == null || role == null || exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
				return Optional.empty();
			}

			return Optional.of(new AuthenticatedUser(username, Role.valueOf(role)));
		} catch (RuntimeException | java.io.IOException exception) {
			return Optional.empty();
		}
	}

	private String encode(Map<String, Object> value) {
		try {
			return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
		} catch (java.io.IOException exception) {
			throw new IllegalStateException("Unable to encode JWT", exception);
		}
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
			return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Unable to sign JWT", exception);
		}
	}
}
