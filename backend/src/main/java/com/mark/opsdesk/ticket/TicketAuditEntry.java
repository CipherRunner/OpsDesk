package com.mark.opsdesk.ticket;

import com.mark.opsdesk.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ticket_audit_entries")
public class TicketAuditEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "actor_id", nullable = false)
	private User actor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private TicketAuditAction action;

	@Column(name = "old_value")
	private String oldValue;

	@Column(name = "new_value")
	private String newValue;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected TicketAuditEntry() {
	}

	TicketAuditEntry(
			Ticket ticket,
			User actor,
			TicketAuditAction action,
			String oldValue,
			String newValue
	) {
		this.ticket = ticket;
		this.actor = actor;
		this.action = action;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public User getActor() {
		return actor;
	}

	public TicketAuditAction getAction() {
		return action;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
