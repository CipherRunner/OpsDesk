package com.mark.opsdesk.ticket.dto;

import com.mark.opsdesk.ticket.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
		@NotBlank
		@Size(max = 255)
		String title,

		@NotBlank
		String description,

		@NotNull
		TicketPriority priority,

		@Size(max = 255)
		String assignedTo
) {
}
