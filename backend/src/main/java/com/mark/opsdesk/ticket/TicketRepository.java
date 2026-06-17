package com.mark.opsdesk.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

	Page<Ticket> findByPriority(TicketPriority priority, Pageable pageable);

	Page<Ticket> findByStatusAndPriority(TicketStatus status, TicketPriority priority, Pageable pageable);

	Page<Ticket> findByCreatedBy(String createdBy, Pageable pageable);

	Page<Ticket> findByCreatedByAndStatus(String createdBy, TicketStatus status, Pageable pageable);

	Page<Ticket> findByCreatedByAndPriority(String createdBy, TicketPriority priority, Pageable pageable);

	Page<Ticket> findByCreatedByAndStatusAndPriority(
			String createdBy,
			TicketStatus status,
			TicketPriority priority,
			Pageable pageable
	);
}
