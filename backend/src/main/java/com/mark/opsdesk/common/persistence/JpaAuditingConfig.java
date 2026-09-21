package com.mark.opsdesk.common.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Turns on Spring Data's auditing listener so {@code @CreatedDate} and {@code @LastModifiedDate}
 * fields on {@link TimestampedEntity} and {@link AuditableEntity} are filled automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
