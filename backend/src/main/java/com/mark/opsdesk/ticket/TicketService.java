package com.mark.opsdesk.ticket;

import com.mark.opsdesk.security.AuthenticatedUser;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.ticket.dto.CreateTicketRequest;
import com.mark.opsdesk.ticket.dto.TicketResponse;
import com.mark.opsdesk.ticket.dto.UpdateTicketAssigneeRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketPriorityRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketStatusRequest;
import com.mark.opsdesk.user.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;
	private final CurrentUserService currentUserService;

	public TicketService(TicketRepository ticketRepository, CurrentUserService currentUserService) {
		this.ticketRepository = ticketRepository;
		this.currentUserService = currentUserService;
	}

	@Transactional
	public TicketResponse createTicket(CreateTicketRequest request) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		if (currentUser.role() == Role.AGENT) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}

		TicketStatus status = request.status() != null ? request.status() : TicketStatus.OPEN;
		Ticket ticket = new Ticket(
				request.title(),
				request.description(),
				status,
				request.priority(),
				currentUser.username(),
				request.assignedTo()
		);

		return toResponse(ticketRepository.save(ticket));
	}

	@Transactional(readOnly = true)
	public Page<TicketResponse> getTickets(TicketStatus status, TicketPriority priority, Pageable pageable) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		if (currentUser.role() == Role.REQUESTER) {
			return getRequesterTickets(currentUser.username(), status, priority, pageable).map(this::toResponse);
		}

		Page<Ticket> tickets;

		if (status != null && priority != null) {
			tickets = ticketRepository.findByStatusAndPriority(status, priority, pageable);
		} else if (status != null) {
			tickets = ticketRepository.findByStatus(status, pageable);
		} else if (priority != null) {
			tickets = ticketRepository.findByPriority(priority, pageable);
		} else {
			tickets = ticketRepository.findAll(pageable);
		}

		return tickets.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicket(Long id) {
		Ticket ticket = findTicket(id);
		ensureCanView(ticket);
		return toResponse(ticket);
	}

	@Transactional
	public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
		currentUserService.requireAnyRole(Role.ADMIN, Role.AGENT);
		Ticket ticket = findTicket(id);
		ticket.updateStatus(request.status());
		return toResponse(ticket);
	}

	@Transactional
	public TicketResponse updateAssignee(Long id, UpdateTicketAssigneeRequest request) {
		currentUserService.requireAnyRole(Role.ADMIN, Role.AGENT);
		Ticket ticket = findTicket(id);
		ticket.updateAssignee(request.assignedTo());
		return toResponse(ticket);
	}

	@Transactional
	public TicketResponse updatePriority(Long id, UpdateTicketPriorityRequest request) {
		currentUserService.requireAnyRole(Role.ADMIN, Role.AGENT);
		Ticket ticket = findTicket(id);
		ticket.updatePriority(request.priority());
		return toResponse(ticket);
	}

	private Ticket findTicket(Long id) {
		return ticketRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
	}

	private Page<Ticket> getRequesterTickets(
			String username,
			TicketStatus status,
			TicketPriority priority,
			Pageable pageable
	) {
		if (status != null && priority != null) {
			return ticketRepository.findByCreatedByAndStatusAndPriority(username, status, priority, pageable);
		}
		if (status != null) {
			return ticketRepository.findByCreatedByAndStatus(username, status, pageable);
		}
		if (priority != null) {
			return ticketRepository.findByCreatedByAndPriority(username, priority, pageable);
		}
		return ticketRepository.findByCreatedBy(username, pageable);
	}

	private void ensureCanView(Ticket ticket) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		if (currentUser.role() == Role.REQUESTER && !ticket.getCreatedBy().equals(currentUser.username())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found");
		}
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
				ticket.getCreatedBy(),
				ticket.getAssignedTo()
		);
	}
}
