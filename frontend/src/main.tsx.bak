import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { KeycloakAuthProvider } from '@/context/KeycloakAuthContext'
import { ThemeProvider } from '@/context/ThemeContext'
import { CurrencyProvider } from '@/context/CurrencyContext'
import { Toaster } from 'sonner'
import App from './App'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <CurrencyProvider>
          <KeycloakAuthProvider>
            <ThemeProvider defaultTheme="light" storageKey="shop-manager-theme">
              <App />
              <Toaster />
            </ThemeProvider>
          </KeycloakAuthProvider>
        </CurrencyProvider>
      </QueryClientProvider>
    </BrowserRouter>
  </React.StrictMode>,
)