package com.mark.opsdesk.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

	/**
	 * Responses always render the creator's and assignee's usernames, so both are fetched with the
	 * ticket instead of one extra query per row.
	 */
	@Override
	@EntityGraph(attributePaths = {"createdBy", "assignedTo"})
	Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);

	@Override
	@EntityGraph(attributePaths = {"createdBy", "assignedTo"})
	Optional<Ticket> findById(Long id);
}
