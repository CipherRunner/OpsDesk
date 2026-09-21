import type { TicketAuditEntry } from '../../api/ticketsApi'
import { formatDateTime } from '../../domain/dates'
import { formatEnumLabel } from '../../domain/ticket'

type AuditListProps = {
  entries: TicketAuditEntry[]
}

function describe(entry: TicketAuditEntry): string {
  const from = entry.oldValue ? formatEnumLabel(entry.oldValue) : null
  const to = entry.newValue ? formatEnumLabel(entry.newValue) : null

  switch (entry.action) {
    case 'TICKET_CREATED':
      return 'created the ticket'
    case 'COMMENT_ADDED':
      return 'added a comment'
    case 'STATUS_CHANGED':
      return `changed status from ${from} to ${to}`
    case 'PRIORITY_CHANGED':
      return `changed priority from ${from} to ${to}`
    case 'ASSIGNEE_CHANGED':
      return entry.oldValue
        ? `reassigned from ${entry.oldValue} to ${entry.newValue}`
        : `assigned to ${entry.newValue}`
    default:
      return formatEnumLabel(entry.action)
  }
}

export function AuditList({ entries }: AuditListProps) {
  if (entries.length === 0) {
    return (
      <div className="empty-state compact">
        <h2>No history yet</h2>
      </div>
    )
  }

  return (
    <ul className="comment-list">
      {entries.map((entry) => (
        <li className="comment-item audit-item" key={entry.id}>
          <div className="comment-meta">
            <span>
              <strong>{entry.actorUsername}</strong> {describe(entry)}
            </span>
            <span>{formatDateTime(entry.createdAt)}</span>
          </div>
        </li>
      ))}
    </ul>
  )
}
