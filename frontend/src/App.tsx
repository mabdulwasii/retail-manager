import React from 'react'
import { Routes, Route } from 'react-router-dom'
import { KeycloakAuthProvider } from '@/context/KeycloakAuthContext'
import { LandingPage } from '@/pages/LandingPage'
import { LoginPage } from '@/pages/auth/LoginPage'
import { RegisterPage } from '@/pages/auth/RegisterPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { AuthenticatedApp } from '@/components/AuthenticatedApp'

function App() {
  return (
    <Routes>
      {/* Public Routes - No authentication required */}
      <Route path="/" element={<LandingPage />} />

      {/* Login route without KeycloakAuthProvider */}
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/register"
        element={
          <KeycloakAuthProvider>
            <RegisterPage />
          </KeycloakAuthProvider>
        }
      />

      {/* All authenticated routes are wrapped in KeycloakAuthProvider */}
      <Route
        path="/dashboard/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/shops/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/products/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/inventory/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/sales/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/receipts/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/investments/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/analytics/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />
      <Route
        path="/audit/*"
        element={
          <KeycloakAuthProvider>
            <AuthenticatedApp />
          </KeycloakAuthProvider>
        }
      />

      {/* 404 Page */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App