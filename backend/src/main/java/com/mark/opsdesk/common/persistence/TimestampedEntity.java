package com.mark.opsdesk.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for append-only rows that only need to know when they were created.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class TimestampedEntity {

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public Instant getCreatedAt() {
		return createdAt;
	}
}
