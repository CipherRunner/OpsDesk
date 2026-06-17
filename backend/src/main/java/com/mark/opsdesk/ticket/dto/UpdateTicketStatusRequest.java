package com.mark.opsdesk.ticket.dto;

import com.mark.opsdesk.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
		@NotNull
		TicketStatus status
) {
}
