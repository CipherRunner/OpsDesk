import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiError'
import type { TicketPriority, TicketStatus } from '../api/ticketsApi'
import { TicketTable } from '../components/tickets/TicketTable'
import { useTickets } from '../queries/tickets'

const statuses: Array<TicketStatus | ''> = [
  '',
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
]

const priorities: Array<TicketPriority | ''> = [
  '',
  'LOW',
  'MEDIUM',
  'HIGH',
  'URGENT',
]

function formatOption(value: string) {
  if (!value) {
    return 'All'
  }

  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

export function TicketsPage() {
  const [statusFilter, setStatusFilter] = useState<TicketStatus | ''>('')
  const [priorityFilter, setPriorityFilter] = useState<TicketPriority | ''>('')
  const navigate = useNavigate()
  const ticketsQuery = useTickets({
    priority: priorityFilter || undefined,
    status: statusFilter || undefined,
  })

  const error = ticketsQuery.error
    ? getApiErrorMessage(ticketsQuery.error, 'Failed to load tickets.')
    : ''

  return (
    <section className="page">
      <div className="page-header page-header-row">
        <div>
          <p className="eyebrow">Queue</p>
          <h1>Tickets</h1>
        </div>

        <Link className="primary-link" to="/tickets/new">
          New ticket
        </Link>
      </div>

      <div className="panel filter-panel">
        <label className="field compact-field">
          Status
          <select
            onChange={(event) =>
              setStatusFilter(event.target.value as TicketStatus | '')
            }
            value={statusFilter}
          >
            {statuses.map((status) => (
              <option key={status || 'all'} value={status}>
                {formatOption(status)}
              </option>
            ))}
          </select>
        </label>

        <label className="field compact-field">
          Priority
          <select
            onChange={(event) =>
              setPriorityFilter(event.target.value as TicketPriority | '')
            }
            value={priorityFilter}
          >
            {priorities.map((priority) => (
              <option key={priority || 'all'} value={priority}>
                {formatOption(priority)}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error ? <p className="form-error">{error}</p> : null}

      {ticketsQuery.isPending ? (
        <div className="panel loading-panel">Loading tickets...</div>
      ) : (
        <TicketTable
          tickets={ticketsQuery.data?.content ?? []}
          onTicketClick={(ticket) => navigate(`/tickets/${ticket.id}`)}
        />
      )}
    </section>
  )
}
