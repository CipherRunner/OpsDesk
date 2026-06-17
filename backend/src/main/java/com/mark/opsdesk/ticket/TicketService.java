package com.mark.opsdesk.ticket;

import com.mark.opsdesk.ticket.dto.CreateTicketRequest;
import com.mark.opsdesk.ticket.dto.TicketResponse;
import com.mark.opsdesk.ticket.dto.UpdateTicketAssigneeRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketPriorityRequest;
import com.mark.opsdesk.ticket.dto.UpdateTicketStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;

	public TicketService(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Transactional
	public TicketResponse createTicket(CreateTicketRequest request) {
		TicketStatus status = request.status() != null ? request.status() : TicketStatus.OPEN;
		Ticket ticket = new Ticket(
				request.title(),
				request.description(),
				status,
				request.priority(),
				request.createdBy(),
				request.assignedTo()
		);

		return toResponse(ticketRepository.save(ticket));
	}

	@Transactional(readOnly = true)
	public Page<TicketResponse> getTickets(TicketStatus status, TicketPriority priority, Pageable pageable) {
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
		return toResponse(findTicket(id));
	}

	@Transactional
	public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
		Ticket ticket = findTicket(id);
		ticket.updateStatus(request.status());
		return toResponse(ticket);
	}

	@Transactional
	public TicketResponse updateAssignee(Long id, UpdateTicketAssigneeRequest request) {
		Ticket ticket = findTicket(id);
		ticket.updateAssignee(request.assignedTo());
		return toResponse(ticket);
	}

	@Transactional
	public TicketResponse updatePriority(Long id, UpdateTicketPriorityRequest request) {
		Ticket ticket = findTicket(id);
		ticket.updatePriority(request.priority());
		return toResponse(ticket);
	}

	private Ticket findTicket(Long id) {
		return ticketRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
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
