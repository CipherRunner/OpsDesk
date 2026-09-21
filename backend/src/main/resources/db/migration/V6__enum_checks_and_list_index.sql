-- Enum columns are stored as VARCHAR (EnumType.STRING). Constrain them at the
-- database level so a bad manual UPDATE or a future code change cannot leave
-- rows the application can no longer map. Adding a value to an enum requires
-- a follow-up migration that replaces the matching constraint.

ALTER TABLE tickets
	ADD CONSTRAINT chk_tickets_status
		CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
	ADD CONSTRAINT chk_tickets_priority
		CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));

ALTER TABLE users
	ADD CONSTRAINT chk_users_role
		CHECK (role IN ('ADMIN', 'AGENT', 'REQUESTER'));

ALTER TABLE ticket_audit_entries
	ADD CONSTRAINT chk_ticket_audit_entries_action
		CHECK (action IN ('TICKET_CREATED', 'STATUS_CHANGED', 'ASSIGNEE_CHANGED', 'PRIORITY_CHANGED', 'COMMENT_ADDED'));

-- The ticket queue is sorted by created_at DESC by default; without this index
-- an unfiltered page is a full sort of the table.
CREATE INDEX idx_tickets_created_at ON tickets (created_at DESC);
