import type { Ticket } from '../../api/ticketsApi'
import { PriorityBadge } from './PriorityBadge'
import { TicketStatusBadge } from './TicketStatusBadge'
import { formatDateTime } from '../../domain/dates'

type TicketTableProps = {
  tickets: Ticket[]
  onTicketClick: (ticket: Ticket) => void
}

export function TicketTable({ tickets, onTicketClick }: TicketTableProps) {
  if (tickets.length === 0) {
    return (
      <div className="empty-state">
        <h2>No tickets found</h2>
        <p>Try changing the filters or create a new ticket.</p>
      </div>
    )
  }

  return (
    <div className="table-wrap">
      <table className="ticket-table">
        <thead>
          <tr>
            <th scope="col">Ticket</th>
            <th scope="col">Status</th>
            <th scope="col">Priority</th>
            <th scope="col">Assigned to</th>
            <th scope="col">Created</th>
          </tr>
        </thead>
        <tbody>
          {tickets.map((ticket) => (
            <tr key={ticket.id}>
              <td>
                <button
                  className="table-link-button"
                  type="button"
                  onClick={() => onTicketClick(ticket)}
                >
                  #{ticket.id} {ticket.title}
                </button>
              </td>
              <td>
                <TicketStatusBadge status={ticket.status} />
              </td>
              <td>
                <PriorityBadge priority={ticket.priority} />
              </td>
              <td>{ticket.assignedTo ?? 'Unassigned'}</td>
              <td>{formatDateTime(ticket.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
