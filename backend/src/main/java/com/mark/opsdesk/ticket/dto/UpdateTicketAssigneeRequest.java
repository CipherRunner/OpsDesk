package com.mark.opsdesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTicketAssigneeRequest(
		@NotBlank
		@Size(max = 255)
		String assignedTo
) {
}
