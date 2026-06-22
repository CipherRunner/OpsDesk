package com.mark.opsdesk.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.mark.opsdesk.IntegrationTestBase;
import com.mark.opsdesk.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketControllerIntegrationTest extends IntegrationTestBase {

	@Autowired
	private TicketRepository ticketRepository;

	private String requesterToken;
	private String agentToken;

	@BeforeEach
	void setUpUsers() throws Exception {
		createTestUser("requester", Role.REQUESTER);
		createTestUser("agent", Role.AGENT);

		requesterToken = login("requester");
		agentToken = login("agent");
	}

	@Test
	void requesterCanCreateTicket() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(requesterToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"title", "Laptop will not boot",
								"description", "The laptop hangs on the vendor logo.",
								"priority", "HIGH"
						))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Laptop will not boot"))
				.andExpect(jsonPath("$.description").value("The laptop hangs on the vendor logo."))
				.andExpect(jsonPath("$.status").value("OPEN"))
				.andExpect(jsonPath("$.priority").value("HIGH"))
				.andExpect(jsonPath("$.createdBy").value("requester"));

		assertThat(ticketRepository.findAll())
				.singleElement()
				.satisfies(ticket -> {
					assertThat(ticket.getTitle()).isEqualTo("Laptop will not boot");
					assertThat(ticket.getCreatedBy()).isEqualTo("requester");
					assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
				});
	}

	@Test
	void ticketTitleIsRequiredAndReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(requesterToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"title", " ",
								"description", "A valid description is present.",
								"priority", "MEDIUM"
						))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("title")));
	}

	@Test
	void agentCanUpdateTicketStatus() throws Exception {
		long ticketId = createTicket(requesterToken, "VPN access is down", TicketStatus.OPEN);

		mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("status", "IN_PROGRESS"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ticketId))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
	}

	@Test
	void filteringByStatusWorks() throws Exception {
		createTicket(requesterToken, "Open monitor issue", TicketStatus.OPEN);
		createTicket(requesterToken, "Resolved printer issue", TicketStatus.RESOLVED);

		mockMvc.perform(get("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.param("status", "RESOLVED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title").value("Resolved printer issue"))
				.andExpect(jsonPath("$.content[0].status").value("RESOLVED"));
	}

	private long createTicket(String token, String title, TicketStatus status) throws Exception {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("title", title);
		request.put("description", title + " description");
		request.put("status", status);
		request.put("priority", TicketPriority.MEDIUM);

		MvcResult result = mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(request)))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.path("id").asLong();
	}
}
