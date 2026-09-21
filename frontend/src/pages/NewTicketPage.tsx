import { Link, useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiError'
import type { CreateTicketRequest } from '../api/ticketsApi'
import { TicketForm } from '../components/tickets/TicketForm'
import { useCreateTicket } from '../queries/tickets'

export function NewTicketPage() {
  const navigate = useNavigate()
  const createTicket = useCreateTicket()

  async function handleCreateTicket(request: CreateTicketRequest) {
    try {
      const ticket = await createTicket.mutateAsync(request)
      navigate(`/tickets/${ticket.id}`)
    } catch {
      // The mutation keeps the error; it is rendered below.
    }
  }

  return (
    <section className="page narrow-page">
      <div className="page-header page-header-row">
        <div>
          <p className="eyebrow">Create</p>
          <h1>New ticket</h1>
        </div>

        <Link className="text-link" to="/tickets">
          Back to tickets
        </Link>
      </div>

      <TicketForm
        error={
          createTicket.error
            ? getApiErrorMessage(createTicket.error, 'Failed to create ticket.')
            : ''
        }
        isSubmitting={createTicket.isPending}
        onSubmit={handleCreateTicket}
      />
    </section>
  )
}
