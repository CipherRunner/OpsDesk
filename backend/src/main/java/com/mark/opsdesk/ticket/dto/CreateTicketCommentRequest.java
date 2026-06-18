package com.mark.opsdesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketCommentRequest(
		@NotBlank
		@Size(max = 2000)
		String content
) {
}
