package com.mark.opsdesk.ticket;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

	/**
	 * Responses render the author's username, so it is joined in instead of loaded per row.
	 */
	@EntityGraph(attributePaths = "author")
	List<TicketComment> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}
