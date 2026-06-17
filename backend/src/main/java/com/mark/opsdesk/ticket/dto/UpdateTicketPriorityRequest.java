package com.mark.opsdesk.ticket.dto;

import com.mark.opsdesk.ticket.TicketPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketPriorityRequest(
		@NotNull
		TicketPriority priority
) {
}
