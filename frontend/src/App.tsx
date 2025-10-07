import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { ManualAuthProvider } from '@/context/ManualAuthContext'
import { LandingPage } from '@/pages/LandingPage'
import { LoginPage } from '@/pages/auth/LoginPage'
import { RegisterPage } from '@/pages/auth/RegisterPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { AuthenticatedApp } from '@/components/AuthenticatedApp'

function App() {
  return (
    <ManualAuthProvider>
      <Routes>
        {/* Public Routes - No authentication required */}
        <Route path="/" element={<LandingPage />} />

        {/* Custom login page */}
        <Route path="/login" element={<LoginPage />} />

        <Route path="/register" element={<RegisterPage />} />

        {/* All authenticated routes */}
        <Route path="/dashboard/*" element={<AuthenticatedApp />} />
        <Route path="/shops/*" element={<AuthenticatedApp />} />
        <Route path="/products/*" element={<AuthenticatedApp />} />
        <Route path="/inventory/*" element={<AuthenticatedApp />} />
        <Route path="/sales/*" element={<AuthenticatedApp />} />
        <Route path="/receipts/*" element={<AuthenticatedApp />} />
        <Route path="/investments/*" element={<AuthenticatedApp />} />
        <Route path="/analytics/*" element={<AuthenticatedApp />} />
        <Route path="/audit/*" element={<AuthenticatedApp />} />

        {/* Profile Route - Redirect to dashboard/profile for consistency */}
        <Route path="/profile" element={<Navigate to="/dashboard/profile" replace />} />

        {/* 404 Page */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </ManualAuthProvider>
  )
}

export default App