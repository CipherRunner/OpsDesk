package com.mark.opsdesk.ticket;

import com.mark.opsdesk.ticket.dto.TicketAuditEntryResponse;
import com.mark.opsdesk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketAuditService {

	private final TicketAuditEntryRepository auditEntryRepository;
	private final TicketAccessPolicy accessPolicy;

	public TicketAuditService(
			TicketAuditEntryRepository auditEntryRepository,
			TicketAccessPolicy accessPolicy
	) {
		this.auditEntryRepository = auditEntryRepository;
		this.accessPolicy = accessPolicy;
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
		Ticket ticket = accessPolicy.requireViewableTicket(ticketId);

		return auditEntryRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId())
				.stream()
				.map(this::toResponse)
				.toList();
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
