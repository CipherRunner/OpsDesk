import { useEffect } from 'react'
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { isAuthenticated, onSessionCleared } from './authStorage'

export function ProtectedRoute() {
  const location = useLocation()
  const navigate = useNavigate()

  useEffect(
    () =>
      onSessionCleared(() => {
        navigate('/login', { replace: true, state: { from: location } })
      }),
    [location, navigate],
  )

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}
