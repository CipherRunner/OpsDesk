package com.mark.opsdesk.ticket;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Ticket lifecycle. Every ticket starts OPEN; the allowed moves are:
 *
 * <pre>
 *   OPEN        -> IN_PROGRESS, RESOLVED, CLOSED
 *   IN_PROGRESS -> OPEN, RESOLVED, CLOSED
 *   RESOLVED    -> IN_PROGRESS, CLOSED      (reopen for more work, or close)
 *   CLOSED      -> OPEN                     (reopen)
 * </pre>
 */
public enum TicketStatus {
	OPEN,
	IN_PROGRESS,
	RESOLVED,
	CLOSED;

	private static final Map<TicketStatus, Set<TicketStatus>> TRANSITIONS = Map.of(
			OPEN, EnumSet.of(IN_PROGRESS, RESOLVED, CLOSED),
			IN_PROGRESS, EnumSet.of(OPEN, RESOLVED, CLOSED),
			RESOLVED, EnumSet.of(IN_PROGRESS, CLOSED),
			CLOSED, EnumSet.of(OPEN)
	);

	public Set<TicketStatus> allowedTransitions() {
		return Collections.unmodifiableSet(TRANSITIONS.get(this));
	}

	public boolean canTransitionTo(TicketStatus target) {
		return TRANSITIONS.get(this).contains(target);
	}
}
