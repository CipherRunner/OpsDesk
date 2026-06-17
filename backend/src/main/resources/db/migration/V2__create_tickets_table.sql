CREATE TABLE tickets (
	id BIGSERIAL PRIMARY KEY,
	title VARCHAR(255) NOT NULL,
	description TEXT NOT NULL,
	status VARCHAR(32) NOT NULL,
	priority VARCHAR(32) NOT NULL,
	created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
	created_by VARCHAR(255) NOT NULL,
	assigned_to VARCHAR(255)
);

CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_priority ON tickets (priority);
