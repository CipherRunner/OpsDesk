package com.mark.opsdesk.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAuditEntryRepository extends JpaRepository<TicketAuditEntry, Long> {

	List<TicketAuditEntry> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}
