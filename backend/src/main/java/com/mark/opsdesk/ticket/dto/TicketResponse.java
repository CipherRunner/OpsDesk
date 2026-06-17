package com.mark.opsdesk.ticket.dto;

import com.mark.opsdesk.ticket.TicketPriority;
import com.mark.opsdesk.ticket.TicketStatus;

import java.time.Instant;

public record TicketResponse(
		Long id,
		String title,
		String description,
		TicketStatus status,
		TicketPriority priority,
		Instant createdAt,
		Instant updatedAt,
		String createdBy,
		String assignedTo
) {
}
