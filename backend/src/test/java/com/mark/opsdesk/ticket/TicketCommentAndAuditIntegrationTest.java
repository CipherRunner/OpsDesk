package com.mark.opsdesk.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.mark.opsdesk.IntegrationTestBase;
import com.mark.opsdesk.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketCommentAndAuditIntegrationTest extends IntegrationTestBase {

	private String ownerToken;
	private String otherRequesterToken;
	private String agentToken;
	private long ticketId;

	@BeforeEach
	void setUp() throws Exception {
		createTestUser("owner", Role.REQUESTER);
		createTestUser("other", Role.REQUESTER);
		createTestUser("agent", Role.AGENT);
		ownerToken = login("owner");
		otherRequesterToken = login("other");
		agentToken = login("agent");

		MvcResult result = mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"title", "Mouse is broken",
								"description", "Left button does not click.",
								"priority", "LOW"
						))))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		ticketId = body.path("id").asLong();
	}

	@Test
	void ownerAndAgentCanCommentAndSeeCommentsInOrder() throws Exception {
		addComment(ownerToken, "Happens since this morning.")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.authorUsername").value("owner"))
				.andExpect(jsonPath("$.ticketId").value(ticketId));

		addComment(agentToken, "Please try another USB port.")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.authorUsername").value("agent"));

		mockMvc.perform(get("/api/tickets/{id}/comments", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].authorUsername", contains("owner", "agent")))
				.andExpect(jsonPath("$[0].content").value("Happens since this morning."));
	}

	@Test
	void otherRequesterCannotSeeOrCommentOnForeignTicket() throws Exception {
		addComment(otherRequesterToken, "I can see this?")
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/tickets/{id}/comments", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(otherRequesterToken)))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/tickets/{id}/audit", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(otherRequesterToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void blankCommentIsRejected() throws Exception {
		addComment(ownerToken, "   ")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors[0].field").value("content"));
	}

	@Test
	void auditTrailRecordsEveryChangeWithActorAndValues() throws Exception {
		addComment(ownerToken, "Any update?").andExpect(status().isCreated());

		mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("status", "IN_PROGRESS"))))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/tickets/{id}/assignee", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("assignedTo", "agent"))))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/tickets/{id}/priority", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("priority", "HIGH"))))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/tickets/{id}/audit", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(5)))
				.andExpect(jsonPath("$[*].action", contains(
						"TICKET_CREATED", "COMMENT_ADDED", "STATUS_CHANGED", "ASSIGNEE_CHANGED", "PRIORITY_CHANGED")))
				.andExpect(jsonPath("$[*].actorUsername", contains("owner", "owner", "agent", "agent", "agent")))
				.andExpect(jsonPath("$[2].oldValue").value("OPEN"))
				.andExpect(jsonPath("$[2].newValue").value("IN_PROGRESS"))
				.andExpect(jsonPath("$[3].oldValue").doesNotExist())
				.andExpect(jsonPath("$[3].newValue").value("agent"))
				.andExpect(jsonPath("$[4].oldValue").value("LOW"))
				.andExpect(jsonPath("$[4].newValue").value("HIGH"));
	}

	@Test
	void unchangedUpdateDoesNotCreateAuditEntry() throws Exception {
		mockMvc.perform(patch("/api/tickets/{id}/priority", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("priority", "LOW"))))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/tickets/{id}/audit", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].action").value("TICKET_CREATED"));
	}

	private ResultActions addComment(String token, String content)
			throws Exception {
		return mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
				.header(HttpHeaders.AUTHORIZATION, bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", content))));
	}
}
