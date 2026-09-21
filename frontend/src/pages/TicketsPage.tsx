import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiError'
import type { TicketPriority, TicketStatus } from '../api/ticketsApi'
import { Pagination } from '../components/tickets/Pagination'
import { TicketTable } from '../components/tickets/TicketTable'
import { TICKET_PRIORITIES, TICKET_STATUSES, formatEnumLabel } from '../domain/ticket'
import { useTickets } from '../queries/tickets'

const statuses: Array<TicketStatus | ''> = ['', ...TICKET_STATUSES]
const priorities: Array<TicketPriority | ''> = ['', ...TICKET_PRIORITIES]

function formatFilterOption(value: string) {
  return value ? formatEnumLabel(value) : 'All'
}

function readStatus(value: string | null): TicketStatus | '' {
  return TICKET_STATUSES.includes(value as TicketStatus) ? (value as TicketStatus) : ''
}

function readPriority(value: string | null): TicketPriority | '' {
  return TICKET_PRIORITIES.includes(value as TicketPriority) ? (value as TicketPriority) : ''
}

const PAGE_SIZE = 20

/** The URL shows pages 1-based for people; the API counts from 0. */
function readPage(value: string | null): number {
  const page = Number(value)

  return Number.isInteger(page) && page >= 1 ? page - 1 : 0
}

/**
 * Filters live in the query string so a filtered queue survives reload and can be shared.
 * Unknown values are treated as "All" rather than sent to the API.
 */
export function TicketsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()

  const statusFilter = readStatus(searchParams.get('status'))
  const priorityFilter = readPriority(searchParams.get('priority'))
  const page = readPage(searchParams.get('page'))

  const ticketsQuery = useTickets({
    priority: priorityFilter || undefined,
    status: statusFilter || undefined,
    page,
    size: PAGE_SIZE,
  })

  function updateParams(changes: Record<string, string | null>) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current)

        for (const [name, value] of Object.entries(changes)) {
          if (value) {
            next.set(name, value)
          } else {
            next.delete(name)
          }
        }

        return next
      },
      { replace: true },
    )
  }

  function updateFilter(name: 'status' | 'priority', value: string) {
    // A new filter means a new result set, so always start from its first page.
    updateParams({ [name]: value, page: null })
  }

  function changePage(nextPage: number) {
    updateParams({ page: nextPage > 0 ? String(nextPage + 1) : null })
  }

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
            onChange={(event) => updateFilter('status', event.target.value)}
            value={statusFilter}
          >
            {statuses.map((status) => (
              <option key={status || 'all'} value={status}>
                {formatFilterOption(status)}
              </option>
            ))}
          </select>
        </label>

        <label className="field compact-field">
          Priority
          <select
            onChange={(event) => updateFilter('priority', event.target.value)}
            value={priorityFilter}
          >
            {priorities.map((priority) => (
              <option key={priority || 'all'} value={priority}>
                {formatFilterOption(priority)}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error ? <p className="form-error">{error}</p> : null}

      {ticketsQuery.isPending ? (
        <div className="panel loading-panel">Loading tickets...</div>
      ) : (
        <>
          <TicketTable
            tickets={ticketsQuery.data?.content ?? []}
            onTicketClick={(ticket) => navigate(`/tickets/${ticket.id}`)}
          />
          <Pagination
            page={ticketsQuery.data?.page ?? page}
            totalPages={ticketsQuery.data?.totalPages ?? 0}
            totalElements={ticketsQuery.data?.totalElements ?? 0}
            onPageChange={changePage}
          />
        </>
      )}
    </section>
  )
}
