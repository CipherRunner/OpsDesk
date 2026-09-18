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
import static org.hamcrest.Matchers.contains;
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
				.andExpect(jsonPath("$.createdBy").value("requester"))
				.andExpect(jsonPath("$.allowedStatusTransitions", contains("IN_PROGRESS", "RESOLVED", "CLOSED")));

		assertThat(ticketRepository.findAll())
				.singleElement()
				.satisfies(ticket -> {
					assertThat(ticket.getTitle()).isEqualTo("Laptop will not boot");
					assertThat(ticket.getCreatedBy()).isEqualTo("requester");
					assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
				});
	}

	@Test
	void statusIsIgnoredOnCreateAndTicketStartsOpen() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(requesterToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"title", "Pre-closed ticket",
								"description", "Client tries to create a ticket that is already closed.",
								"priority", "LOW",
								"status", "CLOSED"
						))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("OPEN"));
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
		long ticketId = createTicket(requesterToken, "VPN access is down");

		mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("status", "IN_PROGRESS"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(ticketId))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.allowedStatusTransitions", contains("OPEN", "RESOLVED", "CLOSED")));

		Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
		assertThat(ticket.getCreatedAt()).isNotNull();
		assertThat(ticket.getUpdatedAt()).isAfter(ticket.getCreatedAt());
	}

	@Test
	void forbiddenStatusTransitionReturnsConflict() throws Exception {
		long ticketId = createTicket(requesterToken, "Printer jams");
		setStatus(ticketId, TicketStatus.RESOLVED);

		mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("status", "OPEN"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value("Cannot change ticket status from RESOLVED to OPEN"));

		assertThat(ticketRepository.findById(ticketId).orElseThrow().getStatus()).isEqualTo(TicketStatus.RESOLVED);
	}

	@Test
	void filteringByStatusWorks() throws Exception {
		createTicket(requesterToken, "Open monitor issue");
		long resolvedId = createTicket(requesterToken, "Resolved printer issue");
		setStatus(resolvedId, TicketStatus.RESOLVED);

		mockMvc.perform(get("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.param("status", "RESOLVED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title").value("Resolved printer issue"))
				.andExpect(jsonPath("$.content[0].status").value("RESOLVED"));
	}

	@Test
	void filteringByPriorityAndStatusCombines() throws Exception {
		long urgentOpen = createTicket(requesterToken, "Urgent open", TicketPriority.URGENT);
		long urgentResolved = createTicket(requesterToken, "Urgent resolved", TicketPriority.URGENT);
		setStatus(urgentResolved, TicketStatus.RESOLVED);
		createTicket(requesterToken, "Low open", TicketPriority.LOW);

		mockMvc.perform(get("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.param("priority", "URGENT"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)));

		mockMvc.perform(get("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.param("priority", "URGENT")
						.param("status", "OPEN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].id").value(urgentOpen));
	}

	@Test
	void requesterListsOnlyOwnTicketsWhileAgentSeesAll() throws Exception {
		createTestUser("other-requester", Role.REQUESTER);
		String otherToken = login("other-requester");
		createTicket(requesterToken, "Mine");
		createTicket(otherToken, "Theirs");

		mockMvc.perform(get("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(requesterToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title").value("Mine"));

		mockMvc.perform(get("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(requesterToken))
						.param("status", "OPEN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)));

		mockMvc.perform(get("/api/tickets")
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)));
	}

	private void setStatus(long ticketId, TicketStatus status) throws Exception {
		mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
						.header(HttpHeaders.AUTHORIZATION, bearer(agentToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of("status", status))))
				.andExpect(status().isOk());
	}

	private long createTicket(String token, String title) throws Exception {
		return createTicket(token, title, TicketPriority.MEDIUM);
	}

	private long createTicket(String token, String title, TicketPriority priority) throws Exception {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("title", title);
		request.put("description", title + " description");
		request.put("priority", priority);

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
