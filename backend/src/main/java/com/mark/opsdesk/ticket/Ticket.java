package com.mark.opsdesk.ticket;

import com.mark.opsdesk.common.persistence.AuditableEntity;
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
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket extends AuditableEntity {

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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_id", nullable = false, updatable = false)
	private User createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_to_id")
	private User assignedTo;

	protected Ticket() {
	}

	public static Ticket create(
			String title,
			String description,
			TicketStatus status,
			TicketPriority priority,
			User createdBy,
			User assignedTo
	) {
		return new Ticket(title, description, status, priority, createdBy, assignedTo);
	}

	Ticket(
			String title,
			String description,
			TicketStatus status,
			TicketPriority priority,
			User createdBy,
			User assignedTo
	) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.priority = priority;
		this.createdBy = createdBy;
		this.assignedTo = assignedTo;
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

	/**
	 * Moves the ticket along its lifecycle. Setting the current status again is a no-op; a move the
	 * lifecycle does not allow fails with {@link InvalidTicketTransitionException}.
	 */
	public void updateStatus(TicketStatus newStatus) {
		if (newStatus == this.status) {
			return;
		}
		if (!this.status.canTransitionTo(newStatus)) {
			throw new InvalidTicketTransitionException(this.status, newStatus);
		}
		this.status = newStatus;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public void updatePriority(TicketPriority priority) {
		this.priority = priority;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public User getAssignedTo() {
		return assignedTo;
	}

	public void updateAssignee(User assignedTo) {
		this.assignedTo = assignedTo;
	}
}
