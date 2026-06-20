import type {
  Ticket,
  TicketPriority,
  TicketStatus,
} from '../../api/ticketsApi'
import type { User } from '../../api/usersApi'
import { PriorityBadge } from './PriorityBadge'
import { TicketStatusBadge } from './TicketStatusBadge'

type TicketDetailProps = {
  assigneeOptions: User[]
  canEditTicketActions: boolean
  isUpdatingAssignee?: boolean
  isUpdatingPriority?: boolean
  isUpdatingStatus?: boolean
  ticket: Ticket
  usersUnavailableMessage?: string
  onAssigneeChange: (assignedTo: string) => Promise<void> | void
  onPriorityChange: (priority: TicketPriority) => Promise<void> | void
  onStatusChange: (status: TicketStatus) => Promise<void> | void
}

const statuses: TicketStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
const priorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function formatOption(value: string) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function formatDate(value: string) {
  return dateFormatter.format(new Date(value))
}

export function TicketDetail({
  assigneeOptions,
  canEditTicketActions,
  isUpdatingAssignee = false,
  isUpdatingPriority = false,
  isUpdatingStatus = false,
  ticket,
  usersUnavailableMessage,
  onAssigneeChange,
  onPriorityChange,
  onStatusChange,
}: TicketDetailProps) {
  const hasCurrentAssigneeOption = assigneeOptions.some(
    (user) => user.username === ticket.assignedTo,
  )

  return (
    <div className="panel ticket-detail">
      <div className="ticket-detail-heading">
        <div>
          <p className="eyebrow">Ticket #{ticket.id}</p>
          <h2>{ticket.title}</h2>
        </div>
        <div className="badge-row">
          <TicketStatusBadge status={ticket.status} />
          <PriorityBadge priority={ticket.priority} />
        </div>
      </div>

      <div className="detail-grid">
        <label className="field">
          Status
          {canEditTicketActions ? (
            <select
              disabled={isUpdatingStatus}
              onChange={(event) =>
                onStatusChange(event.target.value as TicketStatus)
              }
              value={ticket.status}
            >
              {statuses.map((status) => (
                <option key={status} value={status}>
                  {formatOption(status)}
                </option>
              ))}
            </select>
          ) : (
            <span className="readonly-field">{formatOption(ticket.status)}</span>
          )}
        </label>

        <label className="field">
          Priority
          {canEditTicketActions ? (
            <select
              disabled={isUpdatingPriority}
              onChange={(event) =>
                onPriorityChange(event.target.value as TicketPriority)
              }
              value={ticket.priority}
            >
              {priorities.map((priority) => (
                <option key={priority} value={priority}>
                  {formatOption(priority)}
                </option>
              ))}
            </select>
          ) : (
            <span className="readonly-field">
              {formatOption(ticket.priority)}
            </span>
          )}
        </label>

        <label className="field">
          Assignee
          {canEditTicketActions && assigneeOptions.length > 0 ? (
            <select
              disabled={isUpdatingAssignee}
              onChange={(event) => onAssigneeChange(event.target.value)}
              value={ticket.assignedTo ?? ''}
            >
              <option disabled value="">
                Select assignee
              </option>
              {ticket.assignedTo && !hasCurrentAssigneeOption ? (
                <option value={ticket.assignedTo}>{ticket.assignedTo}</option>
              ) : null}
              {assigneeOptions.map((user) => (
                <option key={user.id} value={user.username}>
                  {user.username} ({formatOption(user.role)})
                </option>
              ))}
            </select>
          ) : (
            <span className="readonly-field">
              {ticket.assignedTo ?? 'Unassigned'}
            </span>
          )}
        </label>

        <div className="detail-item">
          <span>Created by</span>
          <strong>{ticket.createdBy}</strong>
        </div>

        <div className="detail-item">
          <span>Created</span>
          <strong>{formatDate(ticket.createdAt)}</strong>
        </div>

        <div className="detail-item">
          <span>Updated</span>
          <strong>{formatDate(ticket.updatedAt)}</strong>
        </div>
      </div>

      {usersUnavailableMessage ? (
        <p className="helper-text">{usersUnavailableMessage}</p>
      ) : null}

      <div className="description-block">
        <h3>Description</h3>
        <p>{ticket.description}</p>
      </div>
    </div>
  )
}
