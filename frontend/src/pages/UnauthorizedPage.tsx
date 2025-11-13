import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { ShieldAlert, ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const UnauthorizedPage: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  
  // Get required roles from navigation state
  const requiredRoles = location.state?.requiredRoles as string[] | undefined
  const message = location.state?.message as string | undefined

  return (
    <div className="flex items-center justify-center min-h-screen bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="rounded-full bg-destructive/10 p-3">
              <ShieldAlert className="h-10 w-10 text-destructive" />
            </div>
          </div>
          <CardTitle className="text-2xl">Access Denied</CardTitle>
          <CardDescription>
            {message || "You don't have permission to access this page."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {requiredRoles && requiredRoles.length > 0 && (
            <div className="rounded-lg bg-muted p-4">
              <p className="text-sm font-medium mb-2">Required Roles:</p>
              <div className="flex flex-wrap gap-2">
                {requiredRoles.map((role) => (
                  <span
                    key={role}
                    className="inline-flex items-center rounded-md bg-primary/10 px-2 py-1 text-xs font-medium text-primary"
                  >
                    {role}
                  </span>
                ))}
              </div>
            </div>
          )}
          <div className="flex flex-col gap-2">
            <Button onClick={() => navigate(-1)} variant="outline" className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Go Back
            </Button>
            <Button onClick={() => navigate('/dashboard')} className="w-full">
              Go to Dashboard
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
