package com.mark.opsdesk.security;

import com.mark.opsdesk.user.User;
import com.mark.opsdesk.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public ApplicationUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return org.springframework.security.core.userdetails.User
				.withUsername(user.getUsername())
				.password(user.getPasswordHash())
				.roles(user.getRole().name())
				.build();
	}
}
