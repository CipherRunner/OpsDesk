package com.mark.opsdesk.user;

import com.mark.opsdesk.user.dto.AuthResponse;
import com.mark.opsdesk.user.dto.CurrentUserResponse;
import com.mark.opsdesk.user.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

	private final AuthService authService;
	private final UserService userService;

	public AuthController(AuthService authService, UserService userService) {
		this.authService = authService;
		this.userService = userService;
	}

	@PostMapping("/auth/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public CurrentUserResponse getCurrentUser() {
		return userService.getCurrentUser();
	}
}
