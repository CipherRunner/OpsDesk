-- Replace the free-text created_by / assigned_to columns on tickets with
-- foreign keys to users. Usernames are resolved to ids; a creator that no
-- longer exists makes the NOT NULL step below fail on purpose, so the
-- operator can decide what to do with orphaned tickets instead of the
-- migration guessing. An assignee that does not match any user becomes
-- NULL (unassigned), since the value was never validated before.

ALTER TABLE tickets ADD COLUMN created_by_id BIGINT;
ALTER TABLE tickets ADD COLUMN assigned_to_id BIGINT;

UPDATE tickets t
SET created_by_id = u.id
FROM users u
WHERE u.username = t.created_by;

UPDATE tickets t
SET assigned_to_id = u.id
FROM users u
WHERE u.username = t.assigned_to;

ALTER TABLE tickets ALTER COLUMN created_by_id SET NOT NULL;

ALTER TABLE tickets
	ADD CONSTRAINT fk_tickets_created_by FOREIGN KEY (created_by_id) REFERENCES users (id),
	ADD CONSTRAINT fk_tickets_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id);

CREATE INDEX idx_tickets_created_by_created_at ON tickets (created_by_id, created_at DESC);
CREATE INDEX idx_tickets_assigned_to ON tickets (assigned_to_id);

ALTER TABLE tickets
	DROP COLUMN created_by,
	DROP COLUMN assigned_to;
