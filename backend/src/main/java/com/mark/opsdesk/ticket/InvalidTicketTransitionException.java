package com.mark.opsdesk.ticket;

import com.mark.opsdesk.common.exception.ConflictException;

public class InvalidTicketTransitionException extends ConflictException {

	public InvalidTicketTransitionException(TicketStatus from, TicketStatus to) {
		super("Cannot change ticket status from " + from + " to " + to);
	}
}
