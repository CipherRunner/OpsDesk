package com.mark.opsdesk.user;

import com.mark.opsdesk.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/users is open to anonymous callers only while the users table is empty, so a fresh
 * deployment can create its first administrator. After that it is ADMIN-only.
 */
class UserBootstrapIntegrationTest extends IntegrationTestBase {

	@Test
	void firstUserMustBeAdmin() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", "first",
								"password", "password123",
								"role", "AGENT"
						))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("First user must be ADMIN"));
	}

	@Test
	void anonymousCanCreateFirstAdminAndThenNobodyElse() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", "root",
								"password", "password123",
								"role", "ADMIN"
						))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("root"))
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", "second",
								"password", "password123",
								"role", "ADMIN"
						))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCreatesUsersAndDuplicatesConflict() throws Exception {
		createTestUser("admin", Role.ADMIN);
		createTestUser("agent", Role.AGENT);
		String adminToken = login("admin");
		String agentToken = login("agent");

		mockMvc.perform(post("/api/users")
						.header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", "newagent",
								"password", "password123",
								"role", "AGENT"
						))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("newagent"));

		mockMvc.perform(post("/api/users")
						.header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", "newagent",
								"password", "password123",
								"role", "AGENT"
						))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Username already exists"));

		mockMvc.perform(post("/api/users")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", "sneaky",
								"password", "password123",
								"role", "ADMIN"
						))))
				.andExpect(status().isForbidden());
	}

	@Test
	void shortPasswordIsRejected() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", "root",
								"password", "short",
								"role", "ADMIN"
						))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
	}
}
