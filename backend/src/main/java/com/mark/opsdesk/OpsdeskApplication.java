package com.mark.opsdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Authentication is stateless JWT (see {@code security} package). Login verifies credentials
 * directly against the users table, so Spring Security's UserDetailsService is never consulted.
 * The auto-configuration is excluded to stop Boot from registering a default in-memory user with a
 * generated password.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class OpsdeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpsdeskApplication.class, args);
	}

}
