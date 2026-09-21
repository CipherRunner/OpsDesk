package com.mark.opsdesk.user;

import com.mark.opsdesk.common.exception.BadRequestException;
import com.mark.opsdesk.common.exception.ConflictException;
import com.mark.opsdesk.common.exception.UnauthorizedException;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.user.dto.CreateUserRequest;
import com.mark.opsdesk.user.dto.CurrentUserResponse;
import com.mark.opsdesk.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final CurrentUserService currentUserService;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			CurrentUserService currentUserService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getUsers() {
		return userRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getAssignableUsers() {
		return userRepository.findByRoleInOrderByUsernameAsc(List.of(Role.ADMIN, Role.AGENT))
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		boolean hasUsers = userRepository.count() > 0;
		if (hasUsers) {
			currentUserService.requireRole(Role.ADMIN);
		} else if (request.role() != Role.ADMIN) {
			throw new BadRequestException("First user must be ADMIN");
		}

		String username = request.username().trim();
		if (userRepository.existsByUsername(username)) {
			throw new ConflictException("Username already exists");
		}

		User user = User.create(
				username,
				passwordEncoder.encode(request.password()),
				request.role()
		);

		return toResponse(userRepository.save(user));
	}

	@Transactional(readOnly = true)
	public CurrentUserResponse getCurrentUser() {
		String username = currentUserService.requireCurrentUser().username();
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UnauthorizedException("User no longer exists"));

		return new CurrentUserResponse(user.getId(), user.getUsername(), user.getRole());
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.getRole(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
