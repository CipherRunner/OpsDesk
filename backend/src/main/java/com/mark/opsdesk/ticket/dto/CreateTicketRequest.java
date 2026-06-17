package com.mark.opsdesk.ticket.dto;

import com.mark.opsdesk.ticket.TicketPriority;
import com.mark.opsdesk.ticket.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
		@NotBlank
		@Size(max = 255)
		String title,

		@NotBlank
		String description,

		TicketStatus status,

		@NotNull
		TicketPriority priority,

		@NotBlank
		@Size(max = 255)
		String createdBy,

		@Size(max = 255)
		String assignedTo
) {
}
