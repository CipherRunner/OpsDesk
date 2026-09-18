package com.mark.opsdesk.ticket;

import org.springframework.data.jpa.domain.Specification;

/**
 * Composable filters for ticket queries. Each factory returns {@code null} when its argument is
 * absent, which {@link Specification#allOf} treats as "no restriction", so callers can pass every
 * optional filter without branching on which ones are set.
 */
final class TicketSpecifications {

	private TicketSpecifications() {
	}

	static Specification<Ticket> hasStatus(TicketStatus status) {
		return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	static Specification<Ticket> hasPriority(TicketPriority priority) {
		return priority == null ? null : (root, query, cb) -> cb.equal(root.get("priority"), priority);
	}

	static Specification<Ticket> createdBy(String username) {
		return username == null ? null : (root, query, cb) -> cb.equal(root.get("createdBy"), username);
	}
}
