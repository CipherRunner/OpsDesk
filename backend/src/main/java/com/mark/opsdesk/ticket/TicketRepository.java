package com.mark.opsdesk.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

	Page<Ticket> findByPriority(TicketPriority priority, Pageable pageable);

	Page<Ticket> findByStatusAndPriority(TicketStatus status, TicketPriority priority, Pageable pageable);
}
