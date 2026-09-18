package com.mark.opsdesk.ticket;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStatusTest {

	@ParameterizedTest
	@CsvSource({
			"OPEN, IN_PROGRESS",
			"OPEN, RESOLVED",
			"OPEN, CLOSED",
			"IN_PROGRESS, OPEN",
			"IN_PROGRESS, RESOLVED",
			"IN_PROGRESS, CLOSED",
			"RESOLVED, IN_PROGRESS",
			"RESOLVED, CLOSED",
			"CLOSED, OPEN",
	})
	void allowedTransitions(TicketStatus from, TicketStatus to) {
		assertThat(from.canTransitionTo(to)).isTrue();
		assertThat(from.allowedTransitions()).contains(to);
	}

	@ParameterizedTest
	@CsvSource({
			"RESOLVED, OPEN",
			"CLOSED, IN_PROGRESS",
			"CLOSED, RESOLVED",
	})
	void forbiddenTransitions(TicketStatus from, TicketStatus to) {
		assertThat(from.canTransitionTo(to)).isFalse();
		assertThat(from.allowedTransitions()).doesNotContain(to);
	}

	@ParameterizedTest
	@EnumSource(TicketStatus.class)
	void statusNeverListsItselfAsTransition(TicketStatus status) {
		assertThat(status.allowedTransitions()).doesNotContain(status);
	}
}
