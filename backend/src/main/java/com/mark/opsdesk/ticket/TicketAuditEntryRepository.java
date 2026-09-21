package com.mark.opsdesk.ticket;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAuditEntryRepository extends JpaRepository<TicketAuditEntry, Long> {

	/**
	 * Responses render the actor's username, so it is joined in instead of loaded per row.
	 */
	@EntityGraph(attributePaths = "actor")
	List<TicketAuditEntry> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}
