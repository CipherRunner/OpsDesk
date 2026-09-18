package com.mark.opsdesk.user;

import com.mark.opsdesk.common.exception.UnauthorizedException;
import com.mark.opsdesk.security.JwtService;
import com.mark.opsdesk.user.dto.AuthResponse;
import com.mark.opsdesk.user.dto.CurrentUserResponse;
import com.mark.opsdesk.user.dto.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByUsername(request.username().trim())
				.orElseThrow(this::invalidCredentials);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw invalidCredentials();
		}

		CurrentUserResponse currentUser = new CurrentUserResponse(user.getId(), user.getUsername(), user.getRole());
		return new AuthResponse(jwtService.createToken(user), currentUser);
	}

	private UnauthorizedException invalidCredentials() {
		return new UnauthorizedException("Invalid username or password");
	}
}
