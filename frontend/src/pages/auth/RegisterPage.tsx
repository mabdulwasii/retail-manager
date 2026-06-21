import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Store, ArrowLeft, Building, User, Mail, Phone, MapPin, Globe } from 'lucide-react'

export const RegisterPage: React.FC = () => {
  const [step, setStep] = useState(1)
  const [registrationType, setRegistrationType] = useState<'cloud' | 'enterprise' | null>(null)
  const [formData, setFormData] = useState({
    // Personal Info
    firstName: '',
    lastName: '',
    email: '',
    phone: '',

    // Company Info
    companyName: '',
    website: '',
    industry: '',
    companySize: '',

    // Address
    address: '',
    city: '',
    state: '',
    country: '',
    zipCode: '',

    // Deployment
    deploymentType: '',
    estimatedShops: '',
    expectedUsers: ''
  })

  const industries = [
    'Retail & Fashion',
    'Electronics',
    'Grocery & Food',
    'Health & Beauty',
    'Sports & Recreation',
    'Books & Media',
    'Home & Garden',
    'Automotive',
    'Other'
  ]

  const companySizes = [
    '1-10 employees',
    '11-50 employees',
    '51-200 employees',
    '201-1000 employees',
    '1000+ employees'
  ]

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleNext = () => {
    setStep(prev => prev + 1)
  }

  const handleBack = () => {
    setStep(prev => prev - 1)
  }

  const handleSubmit = () => {
    // Handle registration submission
    console.log('Registration data:', formData)
    // Redirect to success page or dashboard
  }

  const renderStepIndicator = () => (
    <div className="flex items-center justify-center mb-8">
      {[1, 2, 3, 4].map((num) => (
        <React.Fragment key={num}>
          <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
            step >= num ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-500'
          }`}>
            {num}
          </div>
          {num < 4 && (
            <div className={`w-12 h-1 ${
              step > num ? 'bg-blue-600' : 'bg-gray-200'
            }`} />
          )}
        </React.Fragment>
      ))}
    </div>
  )

  const renderDeploymentSelection = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Choose Your Deployment</h2>
        <p className="text-gray-600">Select the deployment option that best fits your needs</p>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <Card
          className={`cursor-pointer transition-all hover:shadow-lg ${
            registrationType === 'cloud' ? 'ring-2 ring-blue-500 shadow-lg' : ''
          }`}
          onClick={() => setRegistrationType('cloud')}
        >
          <CardHeader className="text-center">
            <Globe className="h-16 w-16 text-blue-600 mx-auto mb-4" />
            <CardTitle>Cloud Deployment</CardTitle>
            <CardDescription>Fully managed SaaS solution</CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2 text-sm">
              <li>✓ Quick setup and onboarding</li>
              <li>✓ Automatic updates and maintenance</li>
              <li>✓ 99.9% uptime guarantee</li>
              <li>✓ Global infrastructure</li>
              <li>✓ Built-in security and compliance</li>
            </ul>
            <div className="mt-4 text-center">
              <div className="text-2xl font-bold text-blue-600">Starting at ₦25,000/month</div>
              <div className="text-sm text-gray-500">30-day free trial</div>
            </div>
          </CardContent>
        </Card>

        <Card
          className={`cursor-pointer transition-all hover:shadow-lg ${
            registrationType === 'enterprise' ? 'ring-2 ring-purple-500 shadow-lg' : ''
          }`}
          onClick={() => setRegistrationType('enterprise')}
        >
          <CardHeader className="text-center">
            <Building className="h-16 w-16 text-purple-600 mx-auto mb-4" />
            <CardTitle>On-Premise Enterprise</CardTitle>
            <CardDescription>Complete control and customization</CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2 text-sm">
              <li>✓ Full data sovereignty</li>
              <li>✓ Custom security policies</li>
              <li>✓ Unlimited customization</li>
              <li>✓ Dedicated support team</li>
              <li>✓ Kubernetes deployment</li>
            </ul>
            <div className="mt-4 text-center">
              <div className="text-2xl font-bold text-purple-600">Custom Pricing</div>
              <div className="text-sm text-gray-500">Contact for quote</div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="flex justify-center">
        <Button
          onClick={handleNext}
          disabled={!registrationType}
          className="px-8"
        >
          Continue
        </Button>
      </div>
    </div>
  )

  const renderPersonalInfo = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Personal Information</h2>
        <p className="text-gray-600">Tell us about yourself</p>
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">First Name *</label>
          <Input
            value={formData.firstName}
            onChange={(e) => handleInputChange('firstName', e.target.value)}
            placeholder="John"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Last Name *</label>
          <Input
            value={formData.lastName}
            onChange={(e) => handleInputChange('lastName', e.target.value)}
            placeholder="Doe"
            required
          />
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Email Address *</label>
        <Input
          type="email"
          value={formData.email}
          onChange={(e) => handleInputChange('email', e.target.value)}
          placeholder="john.doe@company.com"
          required
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Phone Number</label>
        <Input
          type="tel"
          value={formData.phone}
          onChange={(e) => handleInputChange('phone', e.target.value)}
          placeholder="+1 (555) 123-4567"
        />
      </div>

      <div className="flex justify-between">
        <Button variant="outline" onClick={handleBack}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Button
          onClick={handleNext}
          disabled={!formData.firstName || !formData.lastName || !formData.email}
        >
          Continue
        </Button>
      </div>
    </div>
  )

  const renderCompanyInfo = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Company Information</h2>
        <p className="text-gray-600">Tell us about your business</p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Company Name *</label>
        <Input
          value={formData.companyName}
          onChange={(e) => handleInputChange('companyName', e.target.value)}
          placeholder="Acme Retail Corp"
          required
        />
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Industry *</label>
          <select
            value={formData.industry}
            onChange={(e) => handleInputChange('industry', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          >
            <option value="">Select Industry</option>
            {industries.map(industry => (
              <option key={industry} value={industry}>{industry}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Company Size *</label>
          <select
            value={formData.companySize}
            onChange={(e) => handleInputChange('companySize', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          >
            <option value="">Select Size</option>
            {companySizes.map(size => (
              <option key={size} value={size}>{size}</option>
            ))}
          </select>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Website</label>
        <Input
          type="url"
          value={formData.website}
          onChange={(e) => handleInputChange('website', e.target.value)}
          placeholder="https://www.company.com"
        />
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Expected Number of Shops</label>
          <select
            value={formData.estimatedShops}
            onChange={(e) => handleInputChange('estimatedShops', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Select Range</option>
            <option value="1">1 shop</option>
            <option value="2-5">2-5 shops</option>
            <option value="6-20">6-20 shops</option>
            <option value="21-50">21-50 shops</option>
            <option value="50+">50+ shops</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Expected Number of Users</label>
          <select
            value={formData.expectedUsers}
            onChange={(e) => handleInputChange('expectedUsers', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Select Range</option>
            <option value="1-10">1-10 users</option>
            <option value="11-50">11-50 users</option>
            <option value="51-200">51-200 users</option>
            <option value="201-500">201-500 users</option>
            <option value="500+">500+ users</option>
          </select>
        </div>
      </div>

      <div className="flex justify-between">
        <Button variant="outline" onClick={handleBack}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Button
          onClick={handleNext}
          disabled={!formData.companyName || !formData.industry || !formData.companySize}
        >
          Continue
        </Button>
      </div>
    </div>
  )

  const renderReview = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Review & Submit</h2>
        <p className="text-gray-600">Please review your information before submitting</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Registration Summary</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <h4 className="font-semibold text-gray-900">Deployment Type</h4>
            <p className="text-gray-600">
              {registrationType === 'cloud' ? 'Cloud Deployment (SaaS)' : 'On-Premise Enterprise'}
            </p>
          </div>

          <div>
            <h4 className="font-semibold text-gray-900">Contact Information</h4>
            <p className="text-gray-600">
              {formData.firstName} {formData.lastName}<br />
              {formData.email}<br />
              {formData.phone && formData.phone}
            </p>
          </div>

          <div>
            <h4 className="font-semibold text-gray-900">Company Details</h4>
            <p className="text-gray-600">
              {formData.companyName}<br />
              {formData.industry} • {formData.companySize}<br />
              {formData.website && <><a href={formData.website} className="text-blue-600">{formData.website}</a><br /></>}
              Expected: {formData.estimatedShops} shops, {formData.expectedUsers} users
            </p>
          </div>
        </CardContent>
      </Card>

      <div className="bg-blue-50 p-4 rounded-lg">
        <h4 className="font-semibold text-blue-900 mb-2">What happens next?</h4>
        <ul className="text-blue-800 text-sm space-y-1">
          {registrationType === 'cloud' ? (
            <>
              <li>• Instant access to your 30-day free trial</li>
              <li>• Setup wizard to configure your first shop</li>
              <li>• Access to comprehensive documentation and tutorials</li>
              <li>• Optional onboarding call with our success team</li>
            </>
          ) : (
            <>
              <li>• Our enterprise team will contact you within 24 hours</li>
              <li>• Schedule a demo and requirements assessment</li>
              <li>• Receive a custom deployment proposal and quote</li>
              <li>• Technical consultation for implementation planning</li>
            </>
          )}
        </ul>
      </div>

      <div className="text-center">
        <p className="text-sm text-gray-500 mb-4">
          By submitting this form, you agree to our{' '}
          <Link to="/terms" className="text-blue-600 hover:underline">Terms of Service</Link>{' '}
          and{' '}
          <Link to="/privacy" className="text-blue-600 hover:underline">Privacy Policy</Link>.
        </p>
      </div>

      <div className="flex justify-between">
        <Button variant="outline" onClick={handleBack}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Button onClick={handleSubmit} className="px-8">
          {registrationType === 'cloud' ? 'Start Free Trial' : 'Submit Request'}
        </Button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Navigation */}
      <nav className="border-b bg-white/80 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <Link to="/" className="flex items-center space-x-2">
              <Store className="h-8 w-8 text-blue-600" />
              <span className="text-2xl font-bold text-gray-900">RetailHQ</span>
            </Link>

            <div className="text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-blue-600 hover:underline font-medium">
                Sign in
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Registration Form */}
      <div className="py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mx-auto">
          <Card className="shadow-xl">
            <CardContent className="p-8">
              {renderStepIndicator()}

              {step === 1 && renderDeploymentSelection()}
              {step === 2 && renderPersonalInfo()}
              {step === 3 && renderCompanyInfo()}
              {step === 4 && renderReview()}
            </CardContent>
          </Card>

          <div className="text-center mt-8">
            <p className="text-sm text-gray-600">
              Need help with registration?{' '}
              <Link to="/contact" className="text-blue-600 hover:underline">
                Contact our sales team
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}