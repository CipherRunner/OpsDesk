package com.mark.opsdesk.ticket;

import com.mark.opsdesk.common.exception.ForbiddenException;
import com.mark.opsdesk.common.exception.NotFoundException;
import com.mark.opsdesk.security.AuthenticatedUser;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.ticket.dto.CreateTicketRequest;
import com.mark.opsdesk.ticket.dto.TicketResponse;
import com.mark.opsdesk.ticket.dto.UpdateTicketAssigneeRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketStatusRequest;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	private static final Instant CREATED_AT = Instant.parse("2026-06-21T10:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-06-21T10:05:00Z");

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private TicketAccessPolicy accessPolicy;

	@Mock
	private TicketAuditService ticketAuditService;

	@InjectMocks
	private TicketService ticketService;

	@Test
	void createTicketCreatesRequesterTicketWithDefaultOpenStatus() {
		User actor = mock(User.class);
		when(actor.getUsername()).thenReturn("requester");
		when(accessPolicy.requireTicketCreator()).thenReturn(actor);
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			markPersisted(ticket, 42L);
			return ticket;
		});

		TicketResponse response = ticketService.createTicket(new CreateTicketRequest(
				"Laptop will not boot",
				"The laptop hangs on the vendor logo.",
				null,
				TicketPriority.HIGH,
				null
		));

		assertThat(response.id()).isEqualTo(42L);
		assertThat(response.status()).isEqualTo(TicketStatus.OPEN);
		assertThat(response.createdBy()).isEqualTo("requester");

		ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertThat(ticketCaptor.getValue().getTitle()).isEqualTo("Laptop will not boot");
		assertThat(ticketCaptor.getValue().getPriority()).isEqualTo(TicketPriority.HIGH);
		verify(ticketAuditService).record(ticketCaptor.getValue(), actor, TicketAuditAction.TICKET_CREATED, null, null);
	}

	@Test
	void createTicketPropagatesPolicyRejection() {
		when(accessPolicy.requireTicketCreator()).thenThrow(new ForbiddenException("Access denied"));

		assertThatThrownBy(() -> ticketService.createTicket(new CreateTicketRequest(
				"Agent created ticket",
				"Agents are not allowed to create requester tickets.",
				null,
				TicketPriority.MEDIUM,
				null
		)))
				.isInstanceOf(ForbiddenException.class);

		verifyNoInteractions(ticketRepository, ticketAuditService);
	}

	@Test
	void updateStatusRecordsAuditWhenStatusChanges() {
		User actor = mock(User.class);
		Ticket ticket = persistedTicket(7L, "VPN access is down", TicketStatus.OPEN, TicketPriority.MEDIUM, "requester");
		when(accessPolicy.requireTicketManager()).thenReturn(actor);
		when(accessPolicy.requireViewableTicket(7L)).thenReturn(ticket);

		TicketResponse response = ticketService.updateStatus(
				7L,
				new UpdateTicketStatusRequest(TicketStatus.IN_PROGRESS)
		);

		assertThat(response.status()).isEqualTo(TicketStatus.IN_PROGRESS);
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
		verify(ticketAuditService).record(ticket, actor, TicketAuditAction.STATUS_CHANGED, "OPEN", "IN_PROGRESS");
	}

	@Test
	void updateStatusSkipsAuditWhenStatusIsUnchanged() {
		User actor = mock(User.class);
		Ticket ticket = persistedTicket(7L, "VPN access is down", TicketStatus.OPEN, TicketPriority.MEDIUM, "requester");
		when(accessPolicy.requireTicketManager()).thenReturn(actor);
		when(accessPolicy.requireViewableTicket(7L)).thenReturn(ticket);

		ticketService.updateStatus(7L, new UpdateTicketStatusRequest(TicketStatus.OPEN));

		verify(ticketAuditService, never()).record(any(), any(), any(), any(), any());
	}

	@Test
	void updateAssigneeRecordsOldAndNewAssignee() {
		User actor = mock(User.class);
		Ticket ticket = persistedTicket(7L, "VPN access is down", TicketStatus.OPEN, TicketPriority.MEDIUM, "requester");
		when(accessPolicy.requireTicketManager()).thenReturn(actor);
		when(accessPolicy.requireViewableTicket(7L)).thenReturn(ticket);

		ticketService.updateAssignee(7L, new UpdateTicketAssigneeRequest("agent"));

		assertThat(ticket.getAssignedTo()).isEqualTo("agent");
		verify(ticketAuditService).record(ticket, actor, TicketAuditAction.ASSIGNEE_CHANGED, null, "agent");
	}

	@Test
	void updateStatusPropagatesPolicyRejection() {
		when(accessPolicy.requireTicketManager()).thenThrow(new ForbiddenException("Access denied"));

		assertThatThrownBy(() -> ticketService.updateStatus(
				99L,
				new UpdateTicketStatusRequest(TicketStatus.RESOLVED)
		))
				.isInstanceOf(ForbiddenException.class);

		verifyNoInteractions(ticketRepository, ticketAuditService);
	}

	@Test
	void updateStatusPropagatesMissingTicket() {
		when(accessPolicy.requireTicketManager()).thenReturn(mock(User.class));
		when(accessPolicy.requireViewableTicket(404L)).thenThrow(new NotFoundException("Ticket not found"));

		assertThatThrownBy(() -> ticketService.updateStatus(
				404L,
				new UpdateTicketStatusRequest(TicketStatus.RESOLVED)
		))
				.isInstanceOf(NotFoundException.class);

		verifyNoInteractions(ticketAuditService);
	}

	@Test
	void requesterStatusFilterUsesRequesterScopedRepositoryQuery() {
		Pageable pageable = PageRequest.of(0, 20);
		AuthenticatedUser requester = new AuthenticatedUser("requester", Role.REQUESTER);
		Ticket ticket = persistedTicket(11L, "Open monitor issue", TicketStatus.OPEN, TicketPriority.LOW, "requester");
		when(currentUserService.requireCurrentUser()).thenReturn(requester);
		when(accessPolicy.isRequesterScoped(requester)).thenReturn(true);
		when(ticketRepository.findByCreatedByAndStatus("requester", TicketStatus.OPEN, pageable))
				.thenReturn(new PageImpl<>(List.of(ticket), pageable, 1));

		Page<TicketResponse> response = ticketService.getTickets(TicketStatus.OPEN, null, pageable);

		assertThat(response.getContent())
				.singleElement()
				.satisfies(ticketResponse -> {
					assertThat(ticketResponse.title()).isEqualTo("Open monitor issue");
					assertThat(ticketResponse.status()).isEqualTo(TicketStatus.OPEN);
					assertThat(ticketResponse.createdBy()).isEqualTo("requester");
				});
		verify(ticketRepository, never()).findByStatus(any(TicketStatus.class), any(Pageable.class));
	}

	@Test
	void agentStatusFilterUsesUnscopedRepositoryQuery() {
		Pageable pageable = PageRequest.of(0, 20);
		AuthenticatedUser agent = new AuthenticatedUser("agent", Role.AGENT);
		when(currentUserService.requireCurrentUser()).thenReturn(agent);
		when(accessPolicy.isRequesterScoped(agent)).thenReturn(false);
		when(ticketRepository.findByStatus(TicketStatus.OPEN, pageable)).thenReturn(Page.empty(pageable));

		ticketService.getTickets(TicketStatus.OPEN, null, pageable);

		verify(ticketRepository).findByStatus(TicketStatus.OPEN, pageable);
		verify(ticketRepository, never()).findByCreatedByAndStatus(any(), any(), any());
	}

	private static Ticket persistedTicket(
			Long id,
			String title,
			TicketStatus status,
			TicketPriority priority,
			String createdBy
	) {
		Ticket ticket = Ticket.create(title, title + " description", status, priority, createdBy, null);
		markPersisted(ticket, id);
		return ticket;
	}

	private static void markPersisted(Ticket ticket, Long id) {
		ReflectionTestUtils.setField(ticket, "id", id);
		ReflectionTestUtils.setField(ticket, "createdAt", CREATED_AT);
		ReflectionTestUtils.setField(ticket, "updatedAt", UPDATED_AT);
	}
}
