package com.mark.opsdesk.ticket;

import com.mark.opsdesk.common.exception.BadRequestException;
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
import com.mark.opsdesk.user.UserRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
	private UserRepository userRepository;

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
		User actor = user("requester", Role.REQUESTER);
		when(accessPolicy.requireTicketCreator()).thenReturn(actor);
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			markPersisted(ticket, 42L);
			return ticket;
		});

		TicketResponse response = ticketService.createTicket(new CreateTicketRequest(
				"Laptop will not boot",
				"The laptop hangs on the vendor logo.",
				TicketPriority.HIGH,
				null
		));

		assertThat(response.id()).isEqualTo(42L);
		assertThat(response.status()).isEqualTo(TicketStatus.OPEN);
		assertThat(response.createdBy()).isEqualTo("requester");
		assertThat(response.assignedTo()).isNull();
		assertThat(response.allowedStatusTransitions())
				.containsExactly(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED);

		ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertThat(ticketCaptor.getValue().getTitle()).isEqualTo("Laptop will not boot");
		assertThat(ticketCaptor.getValue().getPriority()).isEqualTo(TicketPriority.HIGH);
		assertThat(ticketCaptor.getValue().getCreatedBy()).isSameAs(actor);
		verify(ticketAuditService).record(ticketCaptor.getValue(), actor, TicketAuditAction.TICKET_CREATED, null, null);
		verifyNoInteractions(userRepository);
	}

	@Test
	void createTicketResolvesAssigneeByUsername() {
		User actor = user("admin", Role.ADMIN);
		User agent = user("agent", Role.AGENT);
		when(accessPolicy.requireTicketCreator()).thenReturn(actor);
		when(userRepository.findByUsername("agent")).thenReturn(Optional.of(agent));
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
			Ticket ticket = invocation.getArgument(0);
			markPersisted(ticket, 43L);
			return ticket;
		});

		TicketResponse response = ticketService.createTicket(new CreateTicketRequest(
				"Onboard new hire", "Laptop and accounts.", TicketPriority.MEDIUM, "agent"
		));

		assertThat(response.assignedTo()).isEqualTo("agent");
	}

	@Test
	void createTicketRejectsAssigneeWhoCannotBeAssigned() {
		User admin = user("admin", Role.ADMIN);
		User bob = user("bob", Role.REQUESTER);
		when(accessPolicy.requireTicketCreator()).thenReturn(admin);
		when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

		assertThatThrownBy(() -> ticketService.createTicket(new CreateTicketRequest(
				"Title", "Description", TicketPriority.MEDIUM, "bob"
		)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("must be an admin or agent");

		verifyNoInteractions(ticketRepository, ticketAuditService);
	}

	@Test
	void createTicketPropagatesPolicyRejection() {
		when(accessPolicy.requireTicketCreator()).thenThrow(new ForbiddenException("Access denied"));

		assertThatThrownBy(() -> ticketService.createTicket(new CreateTicketRequest(
				"Agent created ticket",
				"Agents are not allowed to create requester tickets.",
				TicketPriority.MEDIUM,
				null
		)))
				.isInstanceOf(ForbiddenException.class);

		verifyNoInteractions(ticketRepository, ticketAuditService);
	}

	@Test
	void updateStatusRecordsAuditWhenStatusChanges() {
		User actor = user("agent", Role.AGENT);
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
		verify(ticketRepository).flush();
	}

	@Test
	void updateStatusSkipsAuditWhenStatusIsUnchanged() {
		User actor = user("agent", Role.AGENT);
		Ticket ticket = persistedTicket(7L, "VPN access is down", TicketStatus.OPEN, TicketPriority.MEDIUM, "requester");
		when(accessPolicy.requireTicketManager()).thenReturn(actor);
		when(accessPolicy.requireViewableTicket(7L)).thenReturn(ticket);

		ticketService.updateStatus(7L, new UpdateTicketStatusRequest(TicketStatus.OPEN));

		verify(ticketAuditService, never()).record(any(), any(), any(), any(), any());
	}

	@Test
	void updateAssigneeRecordsOldAndNewAssignee() {
		User actor = user("admin", Role.ADMIN);
		User agent = user("agent", Role.AGENT);
		Ticket ticket = persistedTicket(7L, "VPN access is down", TicketStatus.OPEN, TicketPriority.MEDIUM, "requester");
		when(accessPolicy.requireTicketManager()).thenReturn(actor);
		when(accessPolicy.requireViewableTicket(7L)).thenReturn(ticket);
		when(userRepository.findByUsername("agent")).thenReturn(Optional.of(agent));

		TicketResponse response = ticketService.updateAssignee(7L, new UpdateTicketAssigneeRequest("agent"));

		assertThat(ticket.getAssignedTo()).isSameAs(agent);
		assertThat(response.assignedTo()).isEqualTo("agent");
		verify(ticketAuditService).record(ticket, actor, TicketAuditAction.ASSIGNEE_CHANGED, null, "agent");
	}

	@Test
	void updateAssigneeRejectsUnknownUsername() {
		User admin = user("admin", Role.ADMIN);
		Ticket ticket = persistedTicket(7L, "VPN access is down", TicketStatus.OPEN, TicketPriority.MEDIUM, "requester");
		when(accessPolicy.requireTicketManager()).thenReturn(admin);
		when(accessPolicy.requireViewableTicket(7L)).thenReturn(ticket);
		when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.updateAssignee(7L, new UpdateTicketAssigneeRequest("ghost")))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("does not exist");

		assertThat(ticket.getAssignedTo()).isNull();
		verifyNoInteractions(ticketAuditService);
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
		User agent = user("agent", Role.AGENT);
		when(accessPolicy.requireTicketManager()).thenReturn(agent);
		when(accessPolicy.requireViewableTicket(404L)).thenThrow(new NotFoundException("Ticket not found"));

		assertThatThrownBy(() -> ticketService.updateStatus(
				404L,
				new UpdateTicketStatusRequest(TicketStatus.RESOLVED)
		))
				.isInstanceOf(NotFoundException.class);

		verifyNoInteractions(ticketAuditService);
	}

	@Test
	void getTicketsMapsFilteredPageToResponses() {
		Pageable pageable = PageRequest.of(0, 20);
		AuthenticatedUser requester = new AuthenticatedUser("requester", Role.REQUESTER);
		Ticket ticket = persistedTicket(11L, "Open monitor issue", TicketStatus.OPEN, TicketPriority.LOW, "requester");
		when(currentUserService.requireCurrentUser()).thenReturn(requester);
		when(accessPolicy.isRequesterScoped(requester)).thenReturn(true);
		when(ticketRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(ticket), pageable, 1));

		Page<TicketResponse> response = ticketService.getTickets(TicketStatus.OPEN, null, pageable);

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent())
				.singleElement()
				.satisfies(ticketResponse -> {
					assertThat(ticketResponse.id()).isEqualTo(11L);
					assertThat(ticketResponse.title()).isEqualTo("Open monitor issue");
					assertThat(ticketResponse.createdBy()).isEqualTo("requester");
				});
	}

	private static User user(String username, Role role) {
		User user = mock(User.class);
		lenient().when(user.getUsername()).thenReturn(username);
		lenient().when(user.getRole()).thenReturn(role);
		return user;
	}

	private static Ticket persistedTicket(
			Long id,
			String title,
			TicketStatus status,
			TicketPriority priority,
			String createdBy
	) {
		Ticket ticket = Ticket.create(title, title + " description", status, priority, user(createdBy, Role.REQUESTER), null);
		markPersisted(ticket, id);
		return ticket;
	}

	private static void markPersisted(Ticket ticket, Long id) {
		ReflectionTestUtils.setField(ticket, "id", id);
		ReflectionTestUtils.setField(ticket, "createdAt", CREATED_AT);
		ReflectionTestUtils.setField(ticket, "updatedAt", UPDATED_AT);
	}
}
