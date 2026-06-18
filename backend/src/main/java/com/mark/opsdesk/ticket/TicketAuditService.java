package com.mark.opsdesk.ticket;

import com.mark.opsdesk.security.AuthenticatedUser;
import com.mark.opsdesk.security.CurrentUserService;
import com.mark.opsdesk.ticket.dto.TicketAuditEntryResponse;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketAuditService {

	private final TicketAuditEntryRepository auditEntryRepository;
	private final TicketRepository ticketRepository;
	private final CurrentUserService currentUserService;

	public TicketAuditService(
			TicketAuditEntryRepository auditEntryRepository,
			TicketRepository ticketRepository,
			CurrentUserService currentUserService
	) {
		this.auditEntryRepository = auditEntryRepository;
		this.ticketRepository = ticketRepository;
		this.currentUserService = currentUserService;
	}

	@Transactional
	public void record(
			Ticket ticket,
			User actor,
			TicketAuditAction action,
			String oldValue,
			String newValue
	) {
		auditEntryRepository.save(new TicketAuditEntry(ticket, actor, action, oldValue, newValue));
	}

	@Transactional(readOnly = true)
	public List<TicketAuditEntryResponse> getAuditEntries(Long ticketId) {
		Ticket ticket = findTicket(ticketId);
		ensureCanView(ticket);

		return auditEntryRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId())
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private Ticket findTicket(Long id) {
		return ticketRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
	}

	private void ensureCanView(Ticket ticket) {
		AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
		if (currentUser.role() == Role.REQUESTER && !ticket.getCreatedBy().equals(currentUser.username())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found");
		}
	}

	private TicketAuditEntryResponse toResponse(TicketAuditEntry entry) {
		return new TicketAuditEntryResponse(
				entry.getId(),
				entry.getTicket().getId(),
				entry.getActor().getId(),
				entry.getActor().getUsername(),
				entry.getAction(),
				entry.getOldValue(),
				entry.getNewValue(),
				entry.getCreatedAt()
		);
	}
}
