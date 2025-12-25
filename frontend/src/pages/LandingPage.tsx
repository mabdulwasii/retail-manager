import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Store,
  TrendingUp,
  Shield,
  BarChart3,
  Coins,
  Package,
  ArrowRight,
  Check,
  Star,
  Globe,
  Cloud,
  Building,
  Phone,
  Mail,
  MapPin,
  Menu,
  X
} from 'lucide-react'
import { useAuth } from '@/context/UnifiedAuthContext'
import configService from '@/config/runtime-config'

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated, login, logout } = useAuth()
  const isEmbedded = configService.isEmbeddedMode;
  const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false)

  const handleLogin = () => {
    if (isEmbedded) {
      // Redirect to embedded login page
      navigate('/login');
    } else {
      // Keycloak login (redirect to Keycloak)
      login();
    }
  }

  const handleLogout = async () => {
    await logout()
  }

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(prev => !prev)
  }

  // Close mobile menu on navigation
  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false)
  }

  const features = [
    {
      icon: Store,
      title: 'Multi-Shop Management',
      description: 'Manage multiple retail locations from a single platform with comprehensive shop customization and branding.'
    },
    {
      icon: BarChart3,
      title: 'Advanced Analytics',
      description: 'Real-time analytics with revenue tracking, sales insights, and performance metrics across all your shops.'
    },
    {
      icon: Coins,
      title: 'Investment & Profit Sharing',
      description: 'Sophisticated investment tracking with automated profit distribution and ROI calculations.'
    },
    {
      icon: Package,
      title: 'Smart Inventory',
      description: 'Intelligent inventory management with stock alerts, automatic reordering, and product return processing.'
    },
    {
      icon: Shield,
      title: 'Fraud Detection',
      description: 'AI-powered fraud detection and risk management to protect your business from suspicious activities.'
    },
    {
      icon: TrendingUp,
      title: 'Sales Optimization',
      description: 'Streamlined sales processes with receipt generation, transaction tracking, and customer management.'
    }
  ]

  const plans = [
    {
      name: 'Cloud Starter',
      price: '$49',
      period: '/month',
      description: 'Perfect for single shop owners',
      features: [
        'Up to 1 shop',
        '500 products',
        'Basic analytics',
        'Standard support',
        'Cloud hosting'
      ],
      popular: false
    },
    {
      name: 'Cloud Professional',
      price: '$149',
      period: '/month',
      description: 'Ideal for growing businesses',
      features: [
        'Up to 5 shops',
        'Unlimited products',
        'Advanced analytics',
        'Investment tracking',
        'Priority support',
        'Multi-tenant features'
      ],
      popular: true
    },
    {
      name: 'On-Premise Enterprise',
      price: 'Custom',
      period: 'pricing',
      description: 'Complete control for large organizations',
      features: [
        'Unlimited shops',
        'Custom deployment',
        'Dedicated support',
        'Advanced security',
        'API integration',
        'Custom features'
      ],
      popular: false
    }
  ]

  const testimonials = [
    {
      name: 'Sarah Johnson',
      role: 'Retail Chain Owner',
      content: 'Shop Manager transformed how we operate our 12 locations. The investment tracking alone has increased our ROI by 30%.',
      rating: 5
    },
    {
      name: 'Michael Chen',
      role: 'Electronics Store Manager',
      content: 'The fraud detection saved us from multiple suspicious transactions. The analytics help us make data-driven decisions.',
      rating: 5
    },
    {
      name: 'Amanda Rodriguez',
      role: 'Grocery Chain Owner',
      content: 'Multi-tenant features allow us to give each location autonomy while maintaining central oversight. Highly recommended!',
      rating: 5
    }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Navigation */}
      <nav className="border-b bg-white/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-2">
              <Store className="h-8 w-8 text-blue-600" />
              <span className="text-2xl font-bold text-gray-900">Shop Manager</span>
            </div>
            
            {/* Mobile menu button */}
            <div className="md:hidden">
              <Button 
                variant="ghost" 
                size="icon" 
                onClick={toggleMobileMenu}
                aria-label="Toggle mobile menu"
              >
                {isMobileMenuOpen ? 
                  <X className="h-6 w-6" /> : 
                  <Menu className="h-6 w-6" />}
              </Button>
            </div>

            {/* Desktop navigation */}
            <div className="hidden md:flex items-center space-x-8">
              <a href="#features" className="text-gray-600 hover:text-blue-600 transition-colors">Features</a>
              <a href="#pricing" className="text-gray-600 hover:text-blue-600 transition-colors">Pricing</a>
              <a href="#about" className="text-gray-600 hover:text-blue-600 transition-colors">About</a>
              <a href="#contact" className="text-gray-600 hover:text-blue-600 transition-colors">Contact</a>

              {isAuthenticated ? (
                <>
                  <Link to="/redirect" className="text-blue-600 hover:text-blue-700 font-medium">Dashboard</Link>
                  <Button variant="outline" onClick={handleLogout}>
                    Logout
                  </Button>
                </>
              ) : (
                <>
                  <button onClick={handleLogin} className="text-blue-600 hover:text-blue-700 font-medium">Login</button>
                  <Button asChild>
                    <Link to="/register">Get Started</Link>
                  </Button>
                </>
              )}
            </div>
          </div>
          
          {/* Mobile menu - always in DOM for animation purposes */}
          <div 
            className={`md:hidden border-t overflow-hidden transition-all duration-300 ease-in-out ${isMobileMenuOpen ? 'py-4 max-h-96 opacity-100' : 'max-h-0 opacity-0'}`}
          >
              <div className="flex flex-col space-y-4 pb-3">
                <a 
                  href="#features" 
                  className="text-gray-600 hover:text-blue-600 transition-colors px-2 py-1" 
                  onClick={closeMobileMenu}
                >
                  Features
                </a>
                <a 
                  href="#pricing" 
                  className="text-gray-600 hover:text-blue-600 transition-colors px-2 py-1" 
                  onClick={closeMobileMenu}
                >
                  Pricing
                </a>
                <a 
                  href="#about" 
                  className="text-gray-600 hover:text-blue-600 transition-colors px-2 py-1" 
                  onClick={closeMobileMenu}
                >
                  About
                </a>
                <a 
                  href="#contact" 
                  className="text-gray-600 hover:text-blue-600 transition-colors px-2 py-1" 
                  onClick={closeMobileMenu}
                >
                  Contact
                </a>
              </div>
              
              <div className="pt-4 border-t flex flex-col space-y-3">
                {isAuthenticated ? (
                  <>
                    <Link 
                      to="/dashboard" 
                      className="text-blue-600 hover:text-blue-700 font-medium px-2 py-1"
                      onClick={closeMobileMenu}
                    >
                      Dashboard
                    </Link>
                    <Button 
                      variant="outline" 
                      onClick={() => {
                        closeMobileMenu();
                        handleLogout();
                      }}
                      className="w-full justify-center"
                    >
                      Logout
                    </Button>
                  </>
                ) : (
                  <>
                    <Button 
                      variant="ghost" 
                      onClick={() => {
                        closeMobileMenu();
                        handleLogin();
                      }}
                      className="w-full justify-center"
                    >
                      Login
                    </Button>
                    <Button 
                      className="w-full justify-center"
                      onClick={closeMobileMenu}
                      asChild
                    >
                      <Link to="/register">Get Started</Link>
                    </Button>
                  </>
                )}
              </div>
            </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-20 pb-16 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto text-center">
          <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
            Revolutionize Your
            <span className="text-blue-600 block">Retail Management</span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto leading-relaxed">
            Complete multi-tenant retail platform with advanced analytics, investment tracking,
            fraud detection, and intelligent inventory management. Scale from single shop to enterprise.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center mb-12">
            <Button size="lg" className="px-8 py-4 text-lg" asChild>
              <Link to="/register">
                Start Free Trial
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
            <Button variant="outline" size="lg" className="px-8 py-4 text-lg">
              <Link to="/demo">Request Demo</Link>
            </Button>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
            <div>
              <div className="text-3xl font-bold text-blue-600">500+</div>
              <div className="text-gray-600">Active Shops</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-blue-600">$2M+</div>
              <div className="text-gray-600">Transactions Processed</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-blue-600">99.9%</div>
              <div className="text-gray-600">Uptime</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-blue-600">24/7</div>
              <div className="text-gray-600">Support</div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Everything You Need to Succeed
            </h2>
            <p className="text-xl text-gray-600 max-w-3xl mx-auto">
              Comprehensive features designed to streamline operations, boost profitability,
              and scale your retail business efficiently.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature, index) => (
              <Card key={index} className="border-0 shadow-lg hover:shadow-xl transition-shadow">
                <CardHeader>
                  <feature.icon className="h-12 w-12 text-blue-600 mb-4" />
                  <CardTitle className="text-xl">{feature.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <CardDescription className="text-gray-600 leading-relaxed">
                    {feature.description}
                  </CardDescription>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Deployment Options */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Flexible Deployment Options
            </h2>
            <p className="text-xl text-gray-600">
              Choose the deployment model that best fits your business needs
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-12">
            <Card className="shadow-lg">
              <CardHeader className="text-center">
                <Cloud className="h-16 w-16 text-blue-600 mx-auto mb-4" />
                <CardTitle className="text-2xl">Cloud Deployment</CardTitle>
                <CardDescription>Fully managed, scalable, and secure</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <ul className="space-y-3">
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>Multi-tenant SaaS platform</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>Automatic updates and maintenance</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>99.9% uptime guarantee</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>Global CDN and edge locations</span>
                  </li>
                </ul>
                <Button className="w-full mt-6" asChild>
                  <Link to="/register">Start Cloud Trial</Link>
                </Button>
              </CardContent>
            </Card>

            <Card className="shadow-lg">
              <CardHeader className="text-center">
                <Building className="h-16 w-16 text-purple-600 mx-auto mb-4" />
                <CardTitle className="text-2xl">On-Premise</CardTitle>
                <CardDescription>Complete control and customization</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <ul className="space-y-3">
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>Full data sovereignty</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>Custom security policies</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>Kubernetes-ready deployment</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-3" />
                    <span>Enterprise support included</span>
                  </li>
                </ul>
                <Button variant="outline" className="w-full mt-6" asChild>
                  <Link to="/quote">Get Quote</Link>
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Simple, Transparent Pricing
            </h2>
            <p className="text-xl text-gray-600">
              Choose the plan that scales with your business
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {plans.map((plan, index) => (
              <Card key={index} className={`relative ${plan.popular ? 'ring-2 ring-blue-500 shadow-xl' : 'shadow-lg'}`}>
                {plan.popular && (
                  <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                    <span className="bg-blue-500 text-white px-4 py-2 rounded-full text-sm font-medium">
                      Most Popular
                    </span>
                  </div>
                )}
                <CardHeader className="text-center">
                  <CardTitle className="text-2xl">{plan.name}</CardTitle>
                  <div className="flex items-baseline justify-center">
                    <span className="text-4xl font-bold text-gray-900">{plan.price}</span>
                    <span className="text-gray-500 ml-1">{plan.period}</span>
                  </div>
                  <CardDescription>{plan.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-3 mb-6">
                    {plan.features.map((feature, featureIndex) => (
                      <li key={featureIndex} className="flex items-center">
                        <Check className="h-5 w-5 text-green-500 mr-3" />
                        <span>{feature}</span>
                      </li>
                    ))}
                  </ul>
                  <Button
                    className={`w-full ${plan.popular ? 'bg-blue-600 hover:bg-blue-700' : ''}`}
                    variant={plan.popular ? 'default' : 'outline'}
                    asChild
                  >
                    <Link to="/register">
                      {plan.price === 'Custom' ? 'Contact Sales' : 'Start Free Trial'}
                    </Link>
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Trusted by Retail Leaders
            </h2>
            <p className="text-xl text-gray-600">
              See what our customers say about Shop Manager
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {testimonials.map((testimonial, index) => (
              <Card key={index} className="shadow-lg">
                <CardContent className="pt-6">
                  <div className="flex items-center mb-4">
                    {[...Array(testimonial.rating)].map((_, i) => (
                      <Star key={i} className="h-5 w-5 text-yellow-400 fill-current" />
                    ))}
                  </div>
                  <p className="text-gray-600 mb-4 italic">"{testimonial.content}"</p>
                  <div>
                    <div className="font-semibold text-gray-900">{testimonial.name}</div>
                    <div className="text-sm text-gray-500">{testimonial.role}</div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-4xl font-bold text-gray-900 mb-6">
                Built for Modern Retail
              </h2>
              <p className="text-lg text-gray-600 mb-6">
                Shop Manager is a comprehensive retail management platform designed to help
                businesses of all sizes streamline operations, increase profitability, and scale efficiently.
              </p>
              <p className="text-lg text-gray-600 mb-8">
                Our platform combines cutting-edge technology with deep retail expertise to deliver
                a solution that grows with your business. From single shops to enterprise chains,
                we provide the tools you need to succeed.
              </p>
              <div className="grid grid-cols-2 gap-6">
                <div>
                  <div className="text-2xl font-bold text-blue-600">Enterprise</div>
                  <div className="text-gray-600">Grade Security</div>
                </div>
                <div>
                  <div className="text-2xl font-bold text-blue-600">AI-Powered</div>
                  <div className="text-gray-600">Analytics</div>
                </div>
                <div>
                  <div className="text-2xl font-bold text-blue-600">Multi-Tenant</div>
                  <div className="text-gray-600">Architecture</div>
                </div>
                <div>
                  <div className="text-2xl font-bold text-blue-600">24/7</div>
                  <div className="text-gray-600">Support</div>
                </div>
              </div>
            </div>
            <div className="relative">
              <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg p-8 text-white">
                <h3 className="text-2xl font-bold mb-4">Why Choose Shop Manager?</h3>
                <ul className="space-y-3">
                  <li className="flex items-center">
                    <Check className="h-5 w-5 mr-3" />
                    <span>Proven ROI increase of 25-40%</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 mr-3" />
                    <span>Advanced fraud protection</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 mr-3" />
                    <span>Real-time multi-shop analytics</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 mr-3" />
                    <span>Sophisticated investment tracking</span>
                  </li>
                  <li className="flex items-center">
                    <Check className="h-5 w-5 mr-3" />
                    <span>Enterprise-grade security</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Frequently Asked Questions
            </h2>
            <p className="text-xl text-gray-600">
              Get answers to common questions about Shop Manager
            </p>
          </div>

          <div className="space-y-6">
            {[
              {
                q: "What makes Shop Manager different from other retail platforms?",
                a: "Shop Manager offers unique features like multi-tenant architecture, sophisticated investment tracking with profit sharing, AI-powered fraud detection, and comprehensive analytics all in one platform."
              },
              {
                q: "Can I migrate my existing data to Shop Manager?",
                a: "Yes, we provide comprehensive migration tools and dedicated support to help you seamlessly transfer your existing data from other platforms."
              },
              {
                q: "Is my data secure with Shop Manager?",
                a: "Absolutely. We implement enterprise-grade security with encryption at rest and in transit, regular security audits, and compliance with industry standards."
              },
              {
                q: "Do you offer training for my team?",
                a: "Yes, we provide comprehensive training programs, documentation, and ongoing support to ensure your team can effectively use all platform features."
              },
              {
                q: "Can I customize the platform for my specific needs?",
                a: "Yes, especially with our on-premise deployment. We offer extensive customization options and can develop custom features for enterprise clients."
              }
            ].map((faq, index) => (
              <Card key={index} className="shadow-sm">
                <CardContent className="pt-6">
                  <h3 className="font-semibold text-lg text-gray-900 mb-3">{faq.q}</h3>
                  <p className="text-gray-600">{faq.a}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Contact Section */}
      <section id="contact" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Get in Touch
            </h2>
            <p className="text-xl text-gray-600">
              Ready to transform your retail business? Contact our team today.
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-12">
            <div>
              <h3 className="text-2xl font-bold text-gray-900 mb-6">Contact Information</h3>
              <div className="space-y-4">
                <div className="flex items-center">
                  <Phone className="h-6 w-6 text-blue-600 mr-4" />
                  <div>
                    <div className="font-semibold">Phone</div>
                    <div className="text-gray-600">+1 (555) 123-4567</div>
                  </div>
                </div>
                <div className="flex items-center">
                  <Mail className="h-6 w-6 text-blue-600 mr-4" />
                  <div>
                    <div className="font-semibold">Email</div>
                    <div className="text-gray-600">sales@shopmanager.com</div>
                  </div>
                </div>
                <div className="flex items-center">
                  <MapPin className="h-6 w-6 text-blue-600 mr-4" />
                  <div>
                    <div className="font-semibold">Address</div>
                    <div className="text-gray-600">123 Business Ave, Suite 100<br />San Francisco, CA 94107</div>
                  </div>
                </div>
              </div>

              <div className="mt-8">
                <h4 className="text-lg font-semibold text-gray-900 mb-4">Business Hours</h4>
                <div className="space-y-2 text-gray-600">
                  <div>Monday - Friday: 9:00 AM - 6:00 PM PST</div>
                  <div>Saturday: 10:00 AM - 4:00 PM PST</div>
                  <div>Sunday: Closed</div>
                </div>
              </div>
            </div>

            <Card className="shadow-lg">
              <CardHeader>
                <CardTitle>Send us a Message</CardTitle>
                <CardDescription>We'll get back to you within 24 hours</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">First Name</label>
                    <input type="text" className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Last Name</label>
                    <input type="text" className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Email</label>
                  <input type="email" className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Company</label>
                  <input type="text" className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Message</label>
                  <textarea rows={4} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <Button className="w-full">Send Message</Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-blue-600 to-purple-700 text-white">
        <div className="max-w-6xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h2 className="text-4xl font-bold mb-4">
            Ready to Transform Your Retail Business?
          </h2>
          <p className="text-xl mb-8 opacity-90">
            Join hundreds of successful retailers using Shop Manager to increase profitability
            and streamline operations.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" variant="secondary" className="px-8 py-4 text-lg" asChild>
              <Link to="/register">
                Start Free Trial
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
            <Button size="lg" variant="outline" className="px-8 py-4 text-lg border-white text-white hover:bg-white hover:text-blue-600" asChild>
              <Link to="/demo">Request Demo</Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center space-x-2 mb-4">
                <Store className="h-8 w-8 text-blue-400" />
                <span className="text-2xl font-bold">Shop Manager</span>
              </div>
              <p className="text-gray-400">
                The complete retail management platform for modern businesses.
              </p>
            </div>

            <div>
              <h3 className="font-semibold mb-4">Product</h3>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#features" className="hover:text-white transition-colors">Features</a></li>
                <li><a href="#pricing" className="hover:text-white transition-colors">Pricing</a></li>
                <li><Link to="/demo" className="hover:text-white transition-colors">Demo</Link></li>
                <li><Link to="/api-docs" className="hover:text-white transition-colors">API</Link></li>
              </ul>
            </div>

            <div>
              <h3 className="font-semibold mb-4">Company</h3>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#about" className="hover:text-white transition-colors">About</a></li>
                <li><Link to="/careers" className="hover:text-white transition-colors">Careers</Link></li>
                <li><Link to="/blog" className="hover:text-white transition-colors">Blog</Link></li>
                <li><a href="#contact" className="hover:text-white transition-colors">Contact</a></li>
              </ul>
            </div>

            <div>
              <h3 className="font-semibold mb-4">Support</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link to="/docs" className="hover:text-white transition-colors">Documentation</Link></li>
                <li><Link to="/support" className="hover:text-white transition-colors">Help Center</Link></li>
                <li><Link to="/status" className="hover:text-white transition-colors">Status</Link></li>
                <li><Link to="/privacy" className="hover:text-white transition-colors">Privacy</Link></li>
              </ul>
            </div>
          </div>

          <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
            <p>&copy; 2024 Shop Manager. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}