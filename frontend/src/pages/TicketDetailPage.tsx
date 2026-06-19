import { Link, useParams } from 'react-router-dom'

export function TicketDetailPage() {
  const { id } = useParams()

  return (
    <section className="page">
      <div className="page-header">
        <p className="eyebrow">Ticket</p>
        <h1>Ticket {id}</h1>
      </div>

      <div className="placeholder-panel">
        <h2>Ticket detail</h2>
        <p>Detailed ticket content will be added in a later sprint.</p>
        <Link className="text-link" to="/tickets">
          Back to tickets
        </Link>
      </div>
    </section>
  )
}
