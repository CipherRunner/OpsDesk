package com.mark.opsdesk.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.mark.opsdesk.IntegrationTestBase;
import com.mark.opsdesk.ticket.TicketPriority;
import com.mark.opsdesk.ticket.TicketRepository;
import com.mark.opsdesk.ticket.TicketStatus;
import com.mark.opsdesk.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketSecurityIntegrationTest extends IntegrationTestBase {

	@Autowired
	private TicketRepository ticketRepository;

	private String ownerToken;
	private String otherRequesterToken;

	@BeforeEach
	void setUpUsers() throws Exception {
		createTestUser("ticket-owner", Role.REQUESTER);
		createTestUser("other-requester", Role.REQUESTER);

		ownerToken = login("ticket-owner");
		otherRequesterToken = login("other-requester");
	}

	@Test
	void requesterCannotUpdateAnotherUsersTicket() throws Exception {
		long ticketId = createTicket(ownerToken);

		mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(otherRequesterToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("status", "RESOLVED"))))
				.andExpect(status().isForbidden());

		assertThat(ticketRepository.findById(ticketId).orElseThrow().getStatus()).isEqualTo(TicketStatus.OPEN);
	}

	private long createTicket(String token) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"title", "Shared mailbox is unavailable",
								"description", "Requester cannot open the team mailbox.",
								"status", TicketStatus.OPEN,
								"priority", TicketPriority.HIGH
						))))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.path("id").asLong();
	}
}
