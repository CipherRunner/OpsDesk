package com.mark.opsdesk.ticket;

import com.mark.opsdesk.common.exception.ForbiddenException;
import com.mark.opsdesk.common.exception.NotFoundException;
import com.mark.opsdesk.common.exception.UnauthorizedException;
import com.mark.opsdesk.security.AuthenticatedUser;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import com.mark.opsdesk.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAccessPolicyTest {

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TicketRepository ticketRepository;

	@InjectMocks
	private TicketAccessPolicy accessPolicy;

	@ParameterizedTest
	@EnumSource(value = Role.class, names = {"ADMIN", "REQUESTER"})
	void adminAndRequesterMayCreateTickets(Role role) {
		User actor = signIn("alice", role);

		assertThat(accessPolicy.requireTicketCreator()).isSameAs(actor);
	}

	@Test
	void agentMayNotCreateTickets() {
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser("agent", Role.AGENT));

		assertThatThrownBy(accessPolicy::requireTicketCreator).isInstanceOf(ForbiddenException.class);

		verifyNoInteractions(userRepository);
	}

	@ParameterizedTest
	@EnumSource(value = Role.class, names = {"ADMIN", "AGENT"})
	void adminAndAgentMayManageTickets(Role role) {
		User actor = signIn("alice", role);

		assertThat(accessPolicy.requireTicketManager()).isSameAs(actor);
	}

	@Test
	void requesterMayNotManageTickets() {
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser("bob", Role.REQUESTER));

		assertThatThrownBy(accessPolicy::requireTicketManager).isInstanceOf(ForbiddenException.class);

		verifyNoInteractions(userRepository);
	}

	@ParameterizedTest
	@EnumSource(value = Role.class, names = {"ADMIN", "AGENT"})
	void adminAndAgentMayViewAnyTicket(Role role) {
		Ticket ticket = ticketCreatedBy("someone-else");
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser("alice", role));
		when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

		assertThat(accessPolicy.requireViewableTicket(1L)).isSameAs(ticket);
	}

	@Test
	void requesterMayViewOwnTicket() {
		Ticket ticket = ticketCreatedBy("bob");
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser("bob", Role.REQUESTER));
		when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

		assertThat(accessPolicy.requireViewableTicket(1L)).isSameAs(ticket);
	}

	@Test
	void foreignTicketIsHiddenFromRequesterAsNotFound() {
		Ticket ticket = ticketCreatedBy("someone-else");
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser("bob", Role.REQUESTER));
		when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

		assertThatThrownBy(() -> accessPolicy.requireViewableTicket(1L)).isInstanceOf(NotFoundException.class);
	}

	@Test
	void missingTicketIsNotFound() {
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser("alice", Role.ADMIN));
		when(ticketRepository.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> accessPolicy.requireViewableTicket(404L)).isInstanceOf(NotFoundException.class);
	}

	@Test
	void actorWhoseAccountWasDeletedIsUnauthorized() {
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser("ghost", Role.ADMIN));
		when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(accessPolicy::requireActor).isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void onlyRequesterIsScoped() {
		assertThat(accessPolicy.isRequesterScoped(new AuthenticatedUser("r", Role.REQUESTER))).isTrue();
		assertThat(accessPolicy.isRequesterScoped(new AuthenticatedUser("a", Role.AGENT))).isFalse();
		assertThat(accessPolicy.isRequesterScoped(new AuthenticatedUser("a", Role.ADMIN))).isFalse();
	}

	private User signIn(String username, Role role) {
		User actor = mock(User.class);
		when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser(username, role));
		when(userRepository.findByUsername(username)).thenReturn(Optional.of(actor));
		return actor;
	}

	private static Ticket ticketCreatedBy(String username) {
		User creator = mock(User.class);
		lenient().when(creator.getUsername()).thenReturn(username);
		return Ticket.create("Title", "Description", TicketStatus.OPEN, TicketPriority.LOW, creator, null);
	}
}
