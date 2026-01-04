import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { SubscriptionTier } from '@/services/cloudAggregatorService';
import { Check } from 'lucide-react';

interface TierFeature {
  included: boolean;
  label: string;
}

interface TierDetails {
  name: string;
  price: string;
  period: string;
  description: string;
  features: TierFeature[];
  popular?: boolean;
  color: string;
}

const TIER_DETAILS: Record<SubscriptionTier, TierDetails> = {
  [SubscriptionTier.FREE]: {
    name: 'Free',
    price: '$0',
    period: 'forever',
    description: 'Perfect for single store startups',
    color: 'gray',
    features: [
      { included: true, label: '1 shop location' },
      { included: true, label: 'Basic POS features' },
      { included: true, label: 'Up to 1,000 products' },
      { included: true, label: 'Cloud sync (daily)' },
      { included: true, label: 'Email support' },
      { included: false, label: 'Analytics dashboard' },
      { included: false, label: 'Multi-shop management' },
    ],
  },
  [SubscriptionTier.BASIC]: {
    name: 'Basic',
    price: '$29',
    period: '/month',
    description: 'For growing businesses',
    color: 'blue',
    features: [
      { included: true, label: 'Up to 3 shop locations' },
      { included: true, label: 'All POS features' },
      { included: true, label: 'Unlimited products' },
      { included: true, label: 'Cloud sync (hourly)' },
      { included: true, label: 'Basic analytics' },
      { included: true, label: 'Priority email support' },
      { included: false, label: 'Advanced analytics' },
    ],
  },
  [SubscriptionTier.PREMIUM]: {
    name: 'Premium',
    price: '$99',
    period: '/month',
    description: 'For established multi-location businesses',
    color: 'purple',
    popular: true,
    features: [
      { included: true, label: 'Up to 10 shop locations' },
      { included: true, label: 'All Basic features' },
      { included: true, label: 'Advanced analytics & reporting' },
      { included: true, label: 'Cloud sync (real-time)' },
      { included: true, label: 'Export to CSV/PDF' },
      { included: true, label: 'Phone & email support' },
      { included: true, label: 'Custom integrations' },
    ],
  },
  [SubscriptionTier.ENTERPRISE]: {
    name: 'Enterprise',
    price: 'Custom',
    period: 'pricing',
    description: 'For large retail chains',
    color: 'amber',
    features: [
      { included: true, label: 'Unlimited shop locations' },
      { included: true, label: 'All Premium features' },
      { included: true, label: 'Dedicated support team' },
      { included: true, label: 'Custom SLA & uptime guarantee' },
      { included: true, label: 'On-premise deployment option' },
      { included: true, label: 'Custom development' },
      { included: true, label: 'Training & onboarding' },
    ],
  },
};

const COLOR_CLASSES = {
  gray: {
    border: 'border-gray-200',
    badge: 'bg-gray-100 text-gray-800',
    ring: 'ring-gray-500',
    button: 'bg-gray-100 text-gray-900 hover:bg-gray-200',
  },
  blue: {
    border: 'border-blue-200',
    badge: 'bg-blue-100 text-blue-800',
    ring: 'ring-blue-500',
    button: 'bg-blue-600 text-white hover:bg-blue-700',
  },
  purple: {
    border: 'border-purple-200',
    badge: 'bg-purple-100 text-purple-800',
    ring: 'ring-purple-500',
    button: 'bg-purple-600 text-white hover:bg-purple-700',
  },
  amber: {
    border: 'border-amber-200',
    badge: 'bg-amber-100 text-amber-800',
    ring: 'ring-amber-500',
    button: 'bg-amber-600 text-white hover:bg-amber-700',
  },
};

interface SubscriptionTierCardProps {
  tier: SubscriptionTier;
  selected: boolean;
  onSelect: (tier: SubscriptionTier) => void;
}

export const SubscriptionTierCard: React.FC<SubscriptionTierCardProps> = ({
  tier,
  selected,
  onSelect,
}) => {
  const details = TIER_DETAILS[tier];
  const colors = COLOR_CLASSES[details.color as keyof typeof COLOR_CLASSES];

  return (
    <Card
      className={`cursor-pointer transition-all hover:shadow-lg relative ${
        selected ? `ring-2 ${colors.ring} shadow-lg` : colors.border
      }`}
      onClick={() => onSelect(tier)}
    >
      {details.popular && (
        <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
          <span className="bg-gradient-to-r from-purple-600 to-blue-600 text-white text-xs font-semibold px-3 py-1 rounded-full shadow-lg">
            Most Popular
          </span>
        </div>
      )}

      <CardHeader className="text-center pb-4">
        <div className="flex justify-center mb-2">
          <span className={`inline-flex px-3 py-1 rounded-full text-xs font-medium ${colors.badge}`}>
            {details.name}
          </span>
        </div>
        <CardTitle className="text-3xl font-bold">
          {details.price}
          <span className="text-base font-normal text-muted-foreground">{details.period}</span>
        </CardTitle>
        <CardDescription>{details.description}</CardDescription>
      </CardHeader>

      <CardContent>
        <ul className="space-y-3 mb-6">
          {details.features.map((feature, index) => (
            <li key={index} className="flex items-start gap-2 text-sm">
              <Check
                className={`h-5 w-5 flex-shrink-0 ${
                  feature.included ? 'text-green-600' : 'text-gray-300'
                }`}
              />
              <span className={feature.included ? 'text-gray-900' : 'text-gray-400'}>
                {feature.label}
              </span>
            </li>
          ))}
        </ul>

        {selected && (
          <div className="text-center">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-green-700">
              <Check className="h-4 w-4" />
              Selected
            </span>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
