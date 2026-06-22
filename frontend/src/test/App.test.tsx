import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from '../App'
import { setCurrentUserRole, setToken } from '../auth/authStorage'
import { login } from '../api/authApi'
import {
  createTicket,
  getTicket,
  getTicketComments,
  getTickets,
  type Ticket,
  type TicketComment,
} from '../api/ticketsApi'
import { getUsers } from '../api/usersApi'

vi.mock('../api/authApi', () => ({
  login: vi.fn(),
}))

vi.mock('../api/ticketsApi', () => ({
  createTicket: vi.fn(),
  createTicketComment: vi.fn(),
  getTicket: vi.fn(),
  getTicketComments: vi.fn(),
  getTickets: vi.fn(),
  updateTicketAssignee: vi.fn(),
  updateTicketPriority: vi.fn(),
  updateTicketStatus: vi.fn(),
}))

vi.mock('../api/usersApi', () => ({
  getUsers: vi.fn(),
}))

const mockLogin = vi.mocked(login)
const mockCreateTicket = vi.mocked(createTicket)
const mockGetTicket = vi.mocked(getTicket)
const mockGetTicketComments = vi.mocked(getTicketComments)
const mockGetTickets = vi.mocked(getTickets)
const mockGetUsers = vi.mocked(getUsers)

const baseTicket: Ticket = {
  id: 7,
  title: 'VPN access broken',
  description: 'Requester cannot connect to the VPN gateway.',
  status: 'OPEN',
  priority: 'HIGH',
  assignedTo: 'agent.smith',
  createdBy: 'alice',
  createdAt: '2026-06-20T09:15:00Z',
  updatedAt: '2026-06-21T10:30:00Z',
}

function renderApp(initialPath: string) {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <App />
    </MemoryRouter>,
  )
}

function signInAs(role: 'ADMIN' | 'AGENT' | 'REQUESTER' = 'REQUESTER') {
  setToken('test-token')
  setCurrentUserRole(role)
}

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
  mockGetTickets.mockResolvedValue({
    content: [],
    number: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  })
  mockGetTicket.mockResolvedValue(baseTicket)
  mockGetTicketComments.mockResolvedValue([])
  mockGetUsers.mockResolvedValue([])
})

describe('OpsDesk user flows', () => {
  it('renders the login form and submits credentials', async () => {
    const user = userEvent.setup()
    mockLogin.mockResolvedValue({
      token: 'session-token',
      user: {
        id: 1,
        username: 'agent',
        role: 'AGENT',
      },
    })

    renderApp('/login')

    await user.type(screen.getByLabelText(/username/i), 'agent')
    await user.type(screen.getByLabelText(/password/i), 'secret')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(mockLogin).toHaveBeenCalledWith({
      username: 'agent',
      password: 'secret',
    })
    expect(localStorage.getItem('opsdesk_token')).toBe('session-token')
    expect(await screen.findByRole('heading', { name: 'Tickets' })).toBeInTheDocument()
  })

  it('renders loaded tickets on the ticket list page', async () => {
    signInAs()
    mockGetTickets.mockResolvedValue({
      content: [
        baseTicket,
        {
          ...baseTicket,
          id: 8,
          title: 'Email delivery delayed',
          status: 'IN_PROGRESS',
          priority: 'MEDIUM',
          assignedTo: null,
        },
      ],
      number: 0,
      size: 20,
      totalElements: 2,
      totalPages: 1,
    })

    renderApp('/tickets')

    expect(await screen.findByRole('button', { name: /#7 VPN access broken/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /#8 Email delivery delayed/i })).toBeInTheDocument()
    expect(screen.getByText('agent.smith')).toBeInTheDocument()
    expect(screen.getByText('Unassigned')).toBeInTheDocument()
    expect(mockGetTickets).toHaveBeenCalledWith({
      priority: undefined,
      status: undefined,
    })
  })

  it('allows creating a ticket from the new ticket form', async () => {
    const user = userEvent.setup()
    const createdTicket: Ticket = {
      ...baseTicket,
      id: 42,
      title: 'Printer needs toner',
      description: 'The finance printer is faded.',
      priority: 'URGENT',
      assignedTo: null,
      createdBy: 'requester',
    }

    signInAs()
    mockCreateTicket.mockResolvedValue(createdTicket)
    mockGetTicket.mockResolvedValue(createdTicket)

    renderApp('/tickets/new')

    await user.type(screen.getByLabelText(/title/i), '  Printer needs toner  ')
    await user.type(
      screen.getByLabelText(/description/i),
      '  The finance printer is faded.  ',
    )
    await user.selectOptions(screen.getByLabelText(/priority/i), 'URGENT')
    await user.click(screen.getByRole('button', { name: /create ticket/i }))

    expect(mockCreateTicket).toHaveBeenCalledWith({
      title: 'Printer needs toner',
      description: 'The finance printer is faded.',
      priority: 'URGENT',
    })
    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'Printer needs toner',
      }),
    ).toBeInTheDocument()
  })

  it('renders ticket details, comments, and audit fields', async () => {
    const comments: TicketComment[] = [
      {
        id: 100,
        ticketId: baseTicket.id,
        authorId: 2,
        authorUsername: 'agent.smith',
        content: 'Asked the requester for VPN client logs.',
        createdAt: '2026-06-21T11:00:00Z',
      },
    ]

    signInAs()
    mockGetTicket.mockResolvedValue(baseTicket)
    mockGetTicketComments.mockResolvedValue(comments)

    renderApp('/tickets/7')

    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'VPN access broken',
      }),
    ).toBeInTheDocument()
    expect(screen.getByText('Requester cannot connect to the VPN gateway.')).toBeInTheDocument()
    expect(screen.getAllByText('Open').length).toBeGreaterThan(0)
    expect(screen.getAllByText('High').length).toBeGreaterThan(0)
    expect(screen.getAllByText('agent.smith').length).toBeGreaterThan(0)
    expect(screen.getByText('Asked the requester for VPN client logs.')).toBeInTheDocument()

    const detailPanel = screen.getByText('Created by').closest('.ticket-detail')
    expect(detailPanel).not.toBeNull()
    expect(within(detailPanel as HTMLElement).getByText('alice')).toBeInTheDocument()
    expect(within(detailPanel as HTMLElement).getByText('Created')).toBeInTheDocument()
    expect(within(detailPanel as HTMLElement).getByText('Updated')).toBeInTheDocument()
    expect(mockGetTicket).toHaveBeenCalledWith(7)
    expect(mockGetTicketComments).toHaveBeenCalledWith(7)
  })
})
