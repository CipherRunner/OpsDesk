import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { clearSession } from '../auth/authStorage'

const navItems = [
  { to: '/tickets', label: 'Tickets' },
]

export function AppLayout() {
  const navigate = useNavigate()

  function handleLogout() {
    clearSession()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">OD</span>
          <span>OpsDesk</span>
        </div>

        <nav className="sidebar-nav" aria-label="Main navigation">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                isActive ? 'nav-link nav-link-active' : 'nav-link'
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <button className="logout-button" type="button" onClick={handleLogout}>
          Logout
        </button>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
