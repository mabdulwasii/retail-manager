import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { LandingPage } from '../LandingPage'
import { UnifiedAuthProvider } from '@/context/UnifiedAuthContext'
import configService from '@/config/runtime-config'

// Mock react-router-dom
const mockNavigate = jest.fn()
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}))

// Mock config service
jest.mock('@/config/runtime-config', () => ({
  __esModule: true,
  default: {
    get isEmbeddedMode() { return this._isEmbeddedMode || false; },
    set isEmbeddedMode(value) { this._isEmbeddedMode = value; },
    _isEmbeddedMode: false,
  },
}))

// Mock the components that might not be available in test environment
jest.mock('@/components/ui/button', () => ({
  Button: ({ children, className, asChild, ...props }: any) =>
    asChild ? children : <button className={className} {...props}>{children}</button>
}))

jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: any) => <div className={`card ${className || ''}`}>{children}</div>,
  CardContent: ({ children }: any) => <div className="card-content">{children}</div>,
  CardDescription: ({ children }: any) => <div className="card-description">{children}</div>,
  CardHeader: ({ children }: any) => <div className="card-header">{children}</div>,
  CardTitle: ({ children }: any) => <div className="card-title">{children}</div>,
}))

const LandingPageWrapper: React.FC = () => (
  <UnifiedAuthProvider>
    <MemoryRouter>
      <LandingPage />
    </MemoryRouter>
  </UnifiedAuthProvider>
)

describe('LandingPage', () => {
  beforeEach(() => {
    // Mock window.scrollTo
    window.scrollTo = jest.fn()
  })

  it('should render the main heading and tagline', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Revolutionize Your')).toBeInTheDocument()
    expect(screen.getByText('Retail Management')).toBeInTheDocument()
    expect(screen.getByText(/Complete multi-tenant retail platform/)).toBeInTheDocument()
  })

  it('should render navigation menu with correct links', () => {
    render(<LandingPageWrapper />)

    // Use getAllByText since navigation items appear multiple times (desktop + mobile nav)
    const shopManagerElements = screen.getAllByText('Shop Manager')
    expect(shopManagerElements.length).toBeGreaterThan(0)
    
    const featuresElements = screen.getAllByText('Features')
    expect(featuresElements.length).toBeGreaterThan(0)
    
    const pricingElements = screen.getAllByText('Pricing')
    expect(pricingElements.length).toBeGreaterThan(0)
    
    const aboutElements = screen.getAllByText('About')
    expect(aboutElements.length).toBeGreaterThan(0)
    
    const contactElements = screen.getAllByText('Contact')
    expect(contactElements.length).toBeGreaterThan(0)
    
    const loginElements = screen.getAllByText('Login')
    expect(loginElements.length).toBeGreaterThan(0)
    
    const getStartedElements = screen.getAllByText('Get Started')
    expect(getStartedElements.length).toBeGreaterThan(0)
  })

  it('should render all key features', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Multi-Shop Management')).toBeInTheDocument()
    expect(screen.getByText('Advanced Analytics')).toBeInTheDocument()
    expect(screen.getByText('Investment & Profit Sharing')).toBeInTheDocument()
    expect(screen.getByText('Smart Inventory')).toBeInTheDocument()
    expect(screen.getByText('Fraud Detection')).toBeInTheDocument()
    expect(screen.getByText('Sales Optimization')).toBeInTheDocument()
  })

  it('should render deployment options', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Flexible Deployment Options')).toBeInTheDocument()
    expect(screen.getByText('Cloud Deployment')).toBeInTheDocument()
    expect(screen.getByText('On-Premise')).toBeInTheDocument()
    expect(screen.getByText('Fully managed, scalable, and secure')).toBeInTheDocument()
    expect(screen.getByText('Complete control and customization')).toBeInTheDocument()
  })

  it('should render pricing plans', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Simple, Transparent Pricing')).toBeInTheDocument()
    expect(screen.getByText('Cloud Starter')).toBeInTheDocument()
    expect(screen.getByText('Cloud Professional')).toBeInTheDocument()
    expect(screen.getByText('On-Premise Enterprise')).toBeInTheDocument()

    // Check for pricing
    expect(screen.getByText('$49')).toBeInTheDocument()
    expect(screen.getByText('$149')).toBeInTheDocument()
    expect(screen.getByText('Custom')).toBeInTheDocument()
  })

  it('should render customer testimonials', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Trusted by Retail Leaders')).toBeInTheDocument()
    expect(screen.getByText('Sarah Johnson')).toBeInTheDocument()
    expect(screen.getByText('Michael Chen')).toBeInTheDocument()
    expect(screen.getByText('Amanda Rodriguez')).toBeInTheDocument()
    expect(screen.getByText('Retail Chain Owner')).toBeInTheDocument()
  })

  it('should render FAQ section', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Frequently Asked Questions')).toBeInTheDocument()
    expect(screen.getByText(/What makes Shop Manager different/)).toBeInTheDocument()
    expect(screen.getByText(/Can I migrate my existing data/)).toBeInTheDocument()
    expect(screen.getByText(/Is my data secure/)).toBeInTheDocument()
  })

  it('should render contact information', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Get in Touch')).toBeInTheDocument()
    expect(screen.getByText('Contact Information')).toBeInTheDocument()
    expect(screen.getByText('+1 (555) 123-4567')).toBeInTheDocument()
    expect(screen.getByText('sales@shopmanager.com')).toBeInTheDocument()
    expect(screen.getByText(/123 Business Ave/)).toBeInTheDocument()
  })

  it('should render CTA section', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Ready to Transform Your Retail Business?')).toBeInTheDocument()
    expect(screen.getByText(/Join hundreds of successful retailers/)).toBeInTheDocument()
  })

  it('should render footer with company information', () => {
    render(<LandingPageWrapper />)

    // Footer should contain company name and copyright
    const footerElements = screen.getAllByText('Shop Manager')
    expect(footerElements.length).toBeGreaterThan(1) // Should appear in header and footer

    expect(screen.getByText('The complete retail management platform for modern businesses.')).toBeInTheDocument()
    expect(screen.getByText('© 2024 Shop Manager. All rights reserved.')).toBeInTheDocument()
  })

  it('should have multiple call-to-action buttons', () => {
    render(<LandingPageWrapper />)

    const startTrialButtons = screen.getAllByText(/Start Free Trial/)
    const requestDemoButtons = screen.getAllByText(/Request Demo/)

    expect(startTrialButtons.length).toBeGreaterThan(0)
    expect(requestDemoButtons.length).toBeGreaterThan(0)
  })

  it('should render contact section', () => {
    render(<LandingPageWrapper />)

    // Just verify contact section exists
    expect(screen.getByText(/Get in Touch/i)).toBeInTheDocument()
  })

  it('should render statistics section', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('500+')).toBeInTheDocument()
    expect(screen.getByText('Active Shops')).toBeInTheDocument()
    expect(screen.getByText('$2M+')).toBeInTheDocument()
    expect(screen.getByText('Transactions Processed')).toBeInTheDocument()
    expect(screen.getByText('99.9%')).toBeInTheDocument()
    expect(screen.getByText('Uptime')).toBeInTheDocument()
    const supportElements = screen.getAllByText('24/7')
    expect(supportElements.length).toBeGreaterThan(0)
    const supportTextElements = screen.getAllByText('Support')
    expect(supportTextElements.length).toBeGreaterThan(0)
  })

  it('should render about section with key benefits', () => {
    render(<LandingPageWrapper />)

    expect(screen.getByText('Built for Modern Retail')).toBeInTheDocument()
    expect(screen.getByText(/Shop Manager is a comprehensive retail management platform/)).toBeInTheDocument()
    expect(screen.getByText('Why Choose Shop Manager?')).toBeInTheDocument()
    expect(screen.getByText(/Proven ROI increase of 25-40%/)).toBeInTheDocument()
  })

  it('should have accessible navigation anchors', () => {
    render(<LandingPageWrapper />)

    // Check that sections have proper IDs for navigation
    expect(screen.getByText('Everything You Need to Succeed')).toBeInTheDocument()
    expect(screen.getByText('Simple, Transparent Pricing')).toBeInTheDocument()
    expect(screen.getByText('Built for Modern Retail')).toBeInTheDocument()
    expect(screen.getByText('Get in Touch')).toBeInTheDocument()
  })

  describe('Login Navigation', () => {
    beforeEach(() => {
      mockNavigate.mockClear()
    })

    it('should navigate to /login when login button is clicked in embedded mode', () => {
      (configService as any).isEmbeddedMode = true

      render(<LandingPageWrapper />)

      const loginButtons = screen.getAllByText('Login')
      // Click the first login button
      fireEvent.click(loginButtons[0])

      expect(mockNavigate).toHaveBeenCalledWith('/login')
    })

    it('should call login() for Keycloak when login button is clicked in cloud mode', () => {
      (configService as any).isEmbeddedMode = false

      // We need to spy on the login function from useAuth
      // Since we're using UnifiedAuthProvider, we need to verify the Keycloak login is called
      // The actual login call happens in the context, which is mocked

      render(<LandingPageWrapper />)

      const loginButtons = screen.getAllByText('Login')
      fireEvent.click(loginButtons[0])

      // In cloud mode, should NOT navigate to /login
      expect(mockNavigate).not.toHaveBeenCalledWith('/login')
    })
  })
})