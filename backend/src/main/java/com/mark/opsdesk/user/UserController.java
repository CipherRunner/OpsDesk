package com.mark.opsdesk.user;

import com.mark.opsdesk.user.dto.CreateUserRequest;
import com.mark.opsdesk.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserResponse> getUsers() {
		return userService.getUsers();
	}

	@PostMapping
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
	}
}
