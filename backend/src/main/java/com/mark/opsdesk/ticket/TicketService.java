package com.mark.opsdesk.ticket;

import com.mark.opsdesk.common.exception.BadRequestException;
import com.mark.opsdesk.security.AuthenticatedUser;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.ticket.dto.CreateTicketRequest;
import com.mark.opsdesk.ticket.dto.TicketResponse;
import com.mark.opsdesk.ticket.dto.UpdateTicketAssigneeRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketPriorityRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketStatusRequest;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import com.mark.opsdesk.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;
	private final TicketAccessPolicy accessPolicy;
	private final TicketAuditService ticketAuditService;

	public TicketService(
			TicketRepository ticketRepository,
			UserRepository userRepository,
			CurrentUserService currentUserService,
			TicketAccessPolicy accessPolicy,
			TicketAuditService ticketAuditService
	) {
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
		this.currentUserService = currentUserService;
		this.accessPolicy = accessPolicy;
		this.ticketAuditService = ticketAuditService;
	}

	@Transactional
	public TicketResponse createTicket(CreateTicketRequest request) {
		User actor = accessPolicy.requireTicketCreator();
		User assignee = request.assignedTo() == null ? null : resolveAssignee(request.assignedTo());

		Ticket ticket = new Ticket(
				request.title(),
				request.description(),
				TicketStatus.OPEN,
				request.priority(),
				actor,
				assignee
		);

		Ticket savedTicket = ticketRepository.save(ticket);
		ticketAuditService.record(savedTicket, actor, TicketAuditAction.TICKET_CREATED, null, null);

		return toResponse(savedTicket);
	}

	@Transactional(readOnly = true)
	public Page<TicketResponse> getTickets(TicketStatus status, TicketPriority priority, Pageable pageable) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		String scopeToCreator = accessPolicy.isRequesterScoped(currentUser) ? currentUser.username() : null;

		Specification<Ticket> filter = Specification.allOf(
				TicketSpecifications.createdBy(scopeToCreator),
				TicketSpecifications.hasStatus(status),
				TicketSpecifications.hasPriority(priority)
		);

		return ticketRepository.findAll(filter, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicket(Long id) {
		return toResponse(accessPolicy.requireViewableTicket(id));
	}

	@Transactional
	public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
		User actor = accessPolicy.requireTicketManager();
		Ticket ticket = accessPolicy.requireViewableTicket(id);
		TicketStatus oldStatus = ticket.getStatus();
		ticket.updateStatus(request.status());
		if (oldStatus != request.status()) {
			ticketAuditService.record(
					ticket,
					actor,
					TicketAuditAction.STATUS_CHANGED,
					oldStatus.name(),
					request.status().name()
			);
		}
		ticketRepository.flush();
		return toResponse(ticket);
	}

	@Transactional
	public TicketResponse updateAssignee(Long id, UpdateTicketAssigneeRequest request) {
		User actor = accessPolicy.requireTicketManager();
		Ticket ticket = accessPolicy.requireViewableTicket(id);
		User assignee = resolveAssignee(request.assignedTo());
		String oldAssignee = usernameOf(ticket.getAssignedTo());
		ticket.updateAssignee(assignee);
		if (!Objects.equals(oldAssignee, assignee.getUsername())) {
			ticketAuditService.record(
					ticket,
					actor,
					TicketAuditAction.ASSIGNEE_CHANGED,
					oldAssignee,
					assignee.getUsername()
			);
		}
		ticketRepository.flush();
		return toResponse(ticket);
	}

	@Transactional
	public TicketResponse updatePriority(Long id, UpdateTicketPriorityRequest request) {
		User actor = accessPolicy.requireTicketManager();
		Ticket ticket = accessPolicy.requireViewableTicket(id);
		TicketPriority oldPriority = ticket.getPriority();
		ticket.updatePriority(request.priority());
		if (oldPriority != request.priority()) {
			ticketAuditService.record(
					ticket,
					actor,
					TicketAuditAction.PRIORITY_CHANGED,
					oldPriority.name(),
					request.priority().name()
			);
		}
		ticketRepository.flush();
		return toResponse(ticket);
	}

	/**
	 * Only ADMIN and AGENT accounts can be assigned; the input is a username so the API stays
	 * readable, and it is validated here rather than trusted as free text.
	 */
	private User resolveAssignee(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new BadRequestException("Assignee '" + username + "' does not exist"));
		if (user.getRole() != Role.ADMIN && user.getRole() != Role.AGENT) {
			throw new BadRequestException("Assignee '" + username + "' must be an admin or agent");
		}
		return user;
	}

	private static String usernameOf(User user) {
		return user == null ? null : user.getUsername();
	}

	private TicketResponse toResponse(Ticket ticket) {
		return new TicketResponse(
				ticket.getId(),
				ticket.getTitle(),
				ticket.getDescription(),
				ticket.getStatus(),
				ticket.getPriority(),
				ticket.getCreatedAt(),
				ticket.getUpdatedAt(),
				ticket.getCreatedBy().getUsername(),
				usernameOf(ticket.getAssignedTo()),
				List.copyOf(ticket.getStatus().allowedTransitions())
		);
	}
}
