package com.mark.opsdesk.common;

import com.mark.opsdesk.IntegrationTestBase;
import com.mark.opsdesk.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerIntegrationTest extends IntegrationTestBase {

	private String agentToken;

	@BeforeEach
	void setUpUser() throws Exception {
		createTestUser("agent", Role.AGENT);
		agentToken = login("agent");
	}

	@Test
	void unknownEnumFilterValueReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/tickets").param("status", "FOO")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Invalid value 'FOO' for parameter 'status'"))
				.andExpect(jsonPath("$.path").value("/api/tickets"));
	}

	@Test
	void nonNumericPathVariableReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/tickets/{id}", "abc")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'id'"));
	}

	@Test
	void unknownResourceReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/does-not-exist")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.path").value("/api/does-not-exist"));
	}

	@Test
	void unsupportedMethodReturnsMethodNotAllowed() throws Exception {
		mockMvc.perform(delete("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.status").value(405))
				.andExpect(jsonPath("$.error").value("Method Not Allowed"));
	}

	@Test
	void unsupportedMediaTypeReturnsUnsupportedMediaType() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.TEXT_PLAIN)
						.content("not json"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.status").value(415));
	}

	@Test
	void malformedJsonBodyReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{not json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid request body"));
	}
}
