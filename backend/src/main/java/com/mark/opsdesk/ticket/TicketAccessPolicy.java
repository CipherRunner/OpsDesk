package com.mark.opsdesk.ticket;

import com.mark.opsdesk.common.exception.ForbiddenException;
import com.mark.opsdesk.common.exception.NotFoundException;
import com.mark.opsdesk.common.exception.UnauthorizedException;
import com.mark.opsdesk.security.AuthenticatedUser;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import com.mark.opsdesk.user.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Single place for "who may do what with a ticket". Every ticket-facing service asks this policy
 * instead of re-implementing role checks and lookups.
 *
 * <ul>
 *   <li>ADMIN and AGENT manage tickets: change status, priority and assignee.</li>
 *   <li>ADMIN and REQUESTER create tickets; AGENT does not.</li>
 *   <li>REQUESTER sees only tickets they created. Foreign tickets answer 404, not 403, so their
 *       existence is not disclosed.</li>
 * </ul>
 */
@Component
public class TicketAccessPolicy {

	private final CurrentUserService currentUserService;
	private final UserRepository userRepository;
	private final TicketRepository ticketRepository;

	public TicketAccessPolicy(
			CurrentUserService currentUserService,
			UserRepository userRepository,
			TicketRepository ticketRepository
	) {
		this.currentUserService = currentUserService;
		this.userRepository = userRepository;
		this.ticketRepository = ticketRepository;
	}

	/**
	 * The current user as a persisted entity, for attributing comments and audit entries.
	 */
	public User requireActor() {
		return loadActor(currentUserService.requireCurrentUser());
	}

	public User requireTicketCreator() {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		if (currentUser.role() == Role.AGENT) {
			throw new ForbiddenException("Access denied");
		}
		return loadActor(currentUser);
	}

	public User requireTicketManager() {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		if (!canManageTickets(currentUser)) {
			throw new ForbiddenException("Access denied");
		}
		return loadActor(currentUser);
	}

	/**
	 * Loads the ticket and hides it from requesters who did not create it.
	 */
	public Ticket requireViewableTicket(Long ticketId) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		Ticket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new NotFoundException("Ticket not found"));
		if (!canView(currentUser, ticket)) {
			throw new NotFoundException("Ticket not found");
		}
		return ticket;
	}

	public boolean isRequesterScoped(AuthenticatedUser user) {
		return user.role() == Role.REQUESTER;
	}

	private boolean canManageTickets(AuthenticatedUser user) {
		return user.role() == Role.ADMIN || user.role() == Role.AGENT;
	}

	private boolean canView(AuthenticatedUser user, Ticket ticket) {
		return !isRequesterScoped(user) || ticket.getCreatedBy().getUsername().equals(user.username());
	}

	private User loadActor(AuthenticatedUser currentUser) {
		return userRepository.findByUsername(currentUser.username())
				.orElseThrow(() -> new UnauthorizedException("Authentication required"));
	}
}
