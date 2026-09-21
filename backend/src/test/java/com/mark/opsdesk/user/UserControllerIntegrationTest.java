package com.mark.opsdesk.user;

import com.mark.opsdesk.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends IntegrationTestBase {

	@BeforeEach
	void setUpUsers() {
		createTestUser("admin", Role.ADMIN);
		createTestUser("agent", Role.AGENT);
		createTestUser("requester", Role.REQUESTER);
	}

	@Test
	void agentCanListAssignableUsers() throws Exception {
		String agentToken = login("agent");

		mockMvc.perform(get("/api/users/assignable")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].username").value(contains("admin", "agent")));
	}

	@Test
	void adminCanListAssignableUsers() throws Exception {
		String adminToken = login("admin");

		mockMvc.perform(get("/api/users/assignable")
						.header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].username").value(contains("admin", "agent")));
	}

	@Test
	void requesterCannotListAssignableUsers() throws Exception {
		String requesterToken = login("requester");

		mockMvc.perform(get("/api/users/assignable")
						.header(HttpHeaders.AUTHORIZATION, bearer(requesterToken)))
				.andExpect(status().isForbidden());
	}

	@Test
	void agentCannotListAllUsers() throws Exception {
		String agentToken = login("agent");

		mockMvc.perform(get("/api/users")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isForbidden());
	}

	@Test
	void anonymousCannotListAssignableUsers() throws Exception {
		mockMvc.perform(get("/api/users/assignable"))
				.andExpect(status().isUnauthorized());
	}
}
