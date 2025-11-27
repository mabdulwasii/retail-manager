import React, { useEffect } from 'react'
import { Navbar } from './Navbar'
import { Sidebar } from './Sidebar'
import { SidebarProvider, useSidebar } from '@/context/SidebarContext'

interface LayoutProps {
  children: React.ReactNode
}

const LayoutContent: React.FC<LayoutProps> = ({ children }) => {
  const { isOpen, close } = useSidebar()
  
  // Close sidebar on screen resize to desktop
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 1024) {
        close()
      }
    }
    
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [close])

  return (
    <div className="h-screen flex flex-col overflow-hidden bg-background">

      <div className="sticky top-0 z-40">
        <Navbar />
      </div>
      
      <div className="flex flex-1 overflow-hidden">
        {/* Overlay for mobile when sidebar is open */}
        {isOpen && (
          <div 
            className="fixed inset-0 z-20 bg-background/80 backdrop-blur-sm lg:hidden"
            onClick={close}
            aria-hidden="true"
          />
        )}
        
        <div className={`
          fixed inset-y-0 left-0 z-30 w-64 transform overflow-y-auto
          bg-background transition duration-200 ease-in-out
          lg:static lg:translate-x-0 lg:transition-none lg:shrink-0 border-r
          ${isOpen ? 'translate-x-0' : '-translate-x-full'}
        `}>
          <Sidebar />
        </div>
        
        <main className="flex-1 overflow-y-auto p-6 lg:pl-6">
          {children}
        </main>
      </div>
    </div>
  )
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    <SidebarProvider>
      <LayoutContent>{children}</LayoutContent>
    </SidebarProvider>
  )
}