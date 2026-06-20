package com.mark.opsdesk.ticket;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TicketStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TicketPriority priority;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "created_by", nullable = false)
	private String createdBy;

	@Column(name = "assigned_to")
	private String assignedTo;

	protected Ticket() {
	}

	public static Ticket create(
			String title,
			String description,
			TicketStatus status,
			TicketPriority priority,
			String createdBy,
			String assignedTo
	) {
		return new Ticket(title, description, status, priority, createdBy, assignedTo);
	}

	Ticket(
			String title,
			String description,
			TicketStatus status,
			TicketPriority priority,
			String createdBy,
			String assignedTo
	) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.priority = priority;
		this.createdBy = createdBy;
		this.assignedTo = assignedTo;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void updateStatus(TicketStatus status) {
		this.status = status;
		touch();
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public void updatePriority(TicketPriority priority) {
		this.priority = priority;
		touch();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void updateAssignee(String assignedTo) {
		this.assignedTo = assignedTo;
		touch();
	}

	private void touch() {
		this.updatedAt = Instant.now();
	}
}
