package com.mark.opsdesk.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Base class for mutable rows. {@code updatedAt} is refreshed by the auditing listener on flush,
 * so callers that return the entity in the same transaction should flush first if they need the
 * new value.
 */
@MappedSuperclass
public abstract class AuditableEntity extends TimestampedEntity {

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
