import { Link } from 'react-router-dom'

export function TicketsPage() {
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

      <div className="placeholder-panel">
        <h2>Ticket list</h2>
        <p>The full ticket table will be implemented in a later sprint.</p>
      </div>
    </section>
  )
}
