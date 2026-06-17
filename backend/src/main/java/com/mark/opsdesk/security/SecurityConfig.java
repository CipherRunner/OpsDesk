package com.mark.opsdesk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.opsdesk.common.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.objectMapper = objectMapper;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(formLogin -> formLogin.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) ->
								writeError(response, HttpStatus.UNAUTHORIZED, "Authentication required", request))
						.accessDeniedHandler((request, response, exception) ->
								writeError(response, HttpStatus.FORBIDDEN, "Access denied", request))
				)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
						.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private void writeError(
			HttpServletResponse response,
			HttpStatus status,
			String message,
			HttpServletRequest request
	) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ApiErrorResponse body = ApiErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);
		objectMapper.writeValue(response.getWriter(), body);
	}
}
