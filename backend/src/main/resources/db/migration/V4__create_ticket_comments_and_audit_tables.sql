CREATE TABLE ticket_comments (
	id BIGSERIAL PRIMARY KEY,
	ticket_id BIGINT NOT NULL,
	author_id BIGINT NOT NULL,
	content TEXT NOT NULL,
	created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_ticket_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
	CONSTRAINT fk_ticket_comments_author FOREIGN KEY (author_id) REFERENCES users (id),
	CONSTRAINT chk_ticket_comments_content_length CHECK (char_length(content) <= 2000)
);

CREATE INDEX idx_ticket_comments_ticket_created_at ON ticket_comments (ticket_id, created_at, id);
CREATE INDEX idx_ticket_comments_author ON ticket_comments (author_id);

CREATE TABLE ticket_audit_entries (
	id BIGSERIAL PRIMARY KEY,
	ticket_id BIGINT NOT NULL,
	actor_id BIGINT NOT NULL,
	action VARCHAR(64) NOT NULL,
	old_value VARCHAR(255),
	new_value VARCHAR(255),
	created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_ticket_audit_entries_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
	CONSTRAINT fk_ticket_audit_entries_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX idx_ticket_audit_entries_ticket_created_at ON ticket_audit_entries (ticket_id, created_at, id);
CREATE INDEX idx_ticket_audit_entries_actor ON ticket_audit_entries (actor_id);
