package com.mark.opsdesk.ticket.dto;

import java.time.Instant;

public record TicketCommentResponse(
		Long id,
		Long ticketId,
		Long authorId,
		String authorUsername,
		String content,
		Instant createdAt
) {
}
