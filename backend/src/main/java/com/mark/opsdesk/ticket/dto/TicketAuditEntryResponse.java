package com.mark.opsdesk.ticket.dto;

import com.mark.opsdesk.ticket.TicketAuditAction;

import java.time.Instant;

public record TicketAuditEntryResponse(
		Long id,
		Long ticketId,
		Long actorId,
		String actorUsername,
		TicketAuditAction action,
		String oldValue,
		String newValue,
		Instant createdAt
) {
}
