import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/UnifiedAuthContext'
import { Loader2 } from 'lucide-react'

export const AuthCallback: React.FC = () => {
  const navigate = useNavigate()
  const { login, isAuthenticated } = useAuth()

  useEffect(() => {
    const handleAuth = async () => {
      if (isAuthenticated) {
        // Already authenticated, go to dashboard
        navigate('/dashboard')
      } else {
        // Get stored credentials
        const pendingAuth = sessionStorage.getItem('pendingAuth')
        if (pendingAuth) {
          sessionStorage.removeItem('pendingAuth')
          // Trigger Keycloak login with authorization code flow
          await login({
            redirectUri: window.location.origin + '/dashboard'
          })
        } else {
          // No pending auth, redirect to login
          navigate('/login')
        }
      }
    }

    handleAuth()
  }, [isAuthenticated, login, navigate])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <Loader2 className="h-12 w-12 animate-spin text-blue-600 mx-auto mb-4" />
        <h2 className="text-xl font-semibold text-gray-700">Authenticating...</h2>
        <p className="text-gray-500 mt-2">Please wait while we verify your session</p>
      </div>
    </div>
  )
}