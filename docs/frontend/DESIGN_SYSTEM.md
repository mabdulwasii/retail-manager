# Design System

**Version**: 1.0
**Last Updated**: January 2025
**Framework**: TailwindCSS 3.3 + shadcn/ui

---

## Table of Contents

1. [Design Principles](#design-principles)
2. [Color System](#color-system)
3. [Typography](#typography)
4. [Spacing & Layout](#spacing--layout)
5. [Components](#components)
6. [Icons](#icons)
7. [Shadows & Effects](#shadows--effects)
8. [Animations](#animations)
9. [Responsive Design](#responsive-design)
10. [Dark Mode](#dark-mode)
11. [Accessibility](#accessibility)

---

## Design Principles

1. **Clarity**: Clear visual hierarchy and intuitive interactions
2. **Consistency**: Uniform design patterns across all screens
3. **Efficiency**: Minimize clicks and cognitive load
4. **Accessibility**: WCAG 2.1 AA compliance
5. **Responsiveness**: Seamless experience across all devices
6. **Scalability**: Design system that grows with the product

---

## Color System

### Primary Colors

Used for primary actions, links, and brand elements.

```css
/* Tailwind Config */
primary: {
  50:  '#EEF2FF',   /* Very light */
  100: '#E0E7FF',
  200: '#C7D2FE',
  300: '#A5B4FC',
  400: '#818CF8',
  500: '#6366F1',   /* Brand primary */
  600: '#4F46E5',   /* Hover state */
  700: '#4338CA',
  800: '#3730A3',
  900: '#312E81',   /* Very dark */
}
```

**Usage**:
- Primary buttons: `bg-primary-600 hover:bg-primary-700`
- Links: `text-primary-600 hover:text-primary-700`
- Active states: `border-primary-500`

### Secondary Colors

Used for secondary actions and accents.

```css
secondary: {
  50:  '#F5F3FF',
  100: '#EDE9FE',
  200: '#DDD6FE',
  300: '#C4B5FD',
  400: '#A78BFA',
  500: '#8B5CF6',   /* Brand secondary */
  600: '#7C3AED',
  700: '#6D28D9',
  800: '#5B21B6',
  900: '#4C1D95',
}
```

### Neutral Colors

Used for text, backgrounds, and borders.

```css
gray: {
  50:  '#F9FAFB',   /* Lightest background */
  100: '#F3F4F6',   /* Light background */
  200: '#E5E7EB',   /* Border color */
  300: '#D1D5DB',   /* Disabled state */
  400: '#9CA3AF',   /* Placeholder text */
  500: '#6B7280',   /* Secondary text */
  600: '#4B5563',   /* Body text */
  700: '#374151',   /* Headings */
  800: '#1F2937',   /* Dark text */
  900: '#111827',   /* Darkest */
}
```

### Semantic Colors

#### Success (Green)
```css
success: {
  50:  '#ECFDF5',
  100: '#D1FAE5',
  200: '#A7F3D0',
  500: '#10B981',   /* Primary success */
  600: '#059669',   /* Hover */
  900: '#064E3B',
}
```

**Usage**: Success messages, completed states, positive metrics

#### Warning (Amber)
```css
warning: {
  50:  '#FFFBEB',
  100: '#FEF3C7',
  200: '#FDE68A',
  500: '#F59E0B',   /* Primary warning */
  600: '#D97706',   /* Hover */
  900: '#78350F',
}
```

**Usage**: Warning messages, pending states, caution indicators

#### Error (Red)
```css
error: {
  50:  '#FEF2F2',
  100: '#FEE2E2',
  200: '#FECACA',
  500: '#EF4444',   /* Primary error */
  600: '#DC2626',   /* Hover */
  900: '#7F1D1D',
}
```

**Usage**: Error messages, destructive actions, validation errors

#### Info (Blue)
```css
info: {
  50:  '#EFF6FF',
  100: '#DBEAFE',
  200: '#BFDBFE',
  500: '#3B82F6',   /* Primary info */
  600: '#2563EB',   /* Hover */
  900: '#1E3A8A',
}
```

**Usage**: Informational messages, help text, tips

### Color Usage Guidelines

**Text Colors**:
- Primary text: `text-gray-900` (dark mode: `text-gray-100`)
- Secondary text: `text-gray-600` (dark mode: `text-gray-400`)
- Disabled text: `text-gray-400`
- Link text: `text-primary-600`

**Background Colors**:
- Page background: `bg-gray-50`
- Card background: `bg-white`
- Hover background: `bg-gray-100`
- Active background: `bg-gray-200`

**Border Colors**:
- Default border: `border-gray-200`
- Input border: `border-gray-300`
- Focus border: `border-primary-500`
- Error border: `border-error-500`

---

## Typography

### Font Family

```css
fontFamily: {
  sans: ['Inter', 'system-ui', 'sans-serif'],
  mono: ['JetBrains Mono', 'Courier New', 'monospace'],
}
```

### Font Sizes & Line Heights

```css
fontSize: {
  xs:   ['0.75rem', { lineHeight: '1rem' }],     /* 12px / 16px */
  sm:   ['0.875rem', { lineHeight: '1.25rem' }], /* 14px / 20px */
  base: ['1rem', { lineHeight: '1.5rem' }],      /* 16px / 24px */
  lg:   ['1.125rem', { lineHeight: '1.75rem' }], /* 18px / 28px */
  xl:   ['1.25rem', { lineHeight: '1.75rem' }],  /* 20px / 28px */
  '2xl': ['1.5rem', { lineHeight: '2rem' }],     /* 24px / 32px */
  '3xl': ['1.875rem', { lineHeight: '2.25rem' }],/* 30px / 36px */
  '4xl': ['2.25rem', { lineHeight: '2.5rem' }],  /* 36px / 40px */
}
```

### Font Weights

```css
fontWeight: {
  normal: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
}
```

### Typography Scale

#### Headings

```html
<!-- H1 -->
<h1 class="text-4xl font-bold text-gray-900">
  Page Title
</h1>

<!-- H2 -->
<h2 class="text-3xl font-bold text-gray-900">
  Section Heading
</h2>

<!-- H3 -->
<h3 class="text-2xl font-semibold text-gray-900">
  Subsection Heading
</h3>

<!-- H4 -->
<h4 class="text-xl font-semibold text-gray-800">
  Card Title
</h4>

<!-- H5 -->
<h5 class="text-lg font-medium text-gray-800">
  Small Heading
</h5>

<!-- H6 -->
<h6 class="text-base font-medium text-gray-700">
  Label Heading
</h6>
```

#### Body Text

```html
<!-- Large body text -->
<p class="text-lg text-gray-600">
  Large body text for emphasis
</p>

<!-- Regular body text -->
<p class="text-base text-gray-600">
  Regular body text for main content
</p>

<!-- Small body text -->
<p class="text-sm text-gray-500">
  Small text for secondary information
</p>

<!-- Extra small text -->
<p class="text-xs text-gray-500">
  Captions, timestamps, metadata
</p>
```

#### Labels

```html
<!-- Form labels -->
<label class="text-sm font-medium text-gray-700">
  Email Address
</label>

<!-- Badge labels -->
<span class="text-xs font-medium text-gray-700">
  NEW
</span>

<!-- Help text -->
<span class="text-sm text-gray-500">
  Optional helper text
</span>
```

---

## Spacing & Layout

### Spacing Scale

Based on **4px grid system**:

```css
spacing: {
  0:    '0px',
  0.5:  '2px',
  1:    '4px',
  2:    '8px',
  3:    '12px',
  4:    '16px',
  5:    '20px',
  6:    '24px',
  7:    '28px',
  8:    '32px',
  10:   '40px',
  12:   '48px',
  16:   '64px',
  20:   '80px',
  24:   '96px',
  32:   '128px',
}
```

### Common Spacing Patterns

**Component Padding**:
- Small: `p-2` (8px)
- Medium: `p-4` (16px)
- Large: `p-6` (24px)

**Stack Spacing** (vertical gap):
- Tight: `space-y-2` (8px)
- Normal: `space-y-4` (16px)
- Relaxed: `space-y-6` (24px)

**Inline Spacing** (horizontal gap):
- Tight: `space-x-2` (8px)
- Normal: `space-x-4` (16px)
- Relaxed: `space-x-6` (24px)

### Layout Grid

```html
<!-- Container -->
<div class="container mx-auto px-4 sm:px-6 lg:px-8">
  <!-- Max width: 1280px, responsive padding -->
</div>

<!-- Grid layout -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
  <!-- Responsive grid with 24px gap -->
</div>

<!-- Flex layout -->
<div class="flex items-center justify-between gap-4">
  <!-- Flexbox with 16px gap -->
</div>
```

### Border Radius

```css
borderRadius: {
  none: '0px',
  sm:   '0.125rem',  /* 2px */
  DEFAULT: '0.25rem', /* 4px */
  md:   '0.375rem',  /* 6px */
  lg:   '0.5rem',    /* 8px */
  xl:   '0.75rem',   /* 12px */
  '2xl': '1rem',     /* 16px */
  full: '9999px',    /* Pills, circles */
}
```

---

## Components

### Buttons

#### Primary Button

```html
<button class="px-4 py-2 bg-primary-600 text-white font-medium rounded-lg
               hover:bg-primary-700 focus:outline-none focus:ring-2
               focus:ring-primary-500 focus:ring-offset-2
               disabled:opacity-50 disabled:cursor-not-allowed
               transition-colors duration-200">
  Primary Action
</button>
```

#### Secondary Button

```html
<button class="px-4 py-2 bg-white text-gray-700 font-medium rounded-lg
               border border-gray-300
               hover:bg-gray-50 focus:outline-none focus:ring-2
               focus:ring-primary-500 focus:ring-offset-2
               disabled:opacity-50 disabled:cursor-not-allowed
               transition-colors duration-200">
  Secondary Action
</button>
```

#### Destructive Button

```html
<button class="px-4 py-2 bg-error-600 text-white font-medium rounded-lg
               hover:bg-error-700 focus:outline-none focus:ring-2
               focus:ring-error-500 focus:ring-offset-2
               disabled:opacity-50 disabled:cursor-not-allowed
               transition-colors duration-200">
  Delete
</button>
```

#### Ghost Button

```html
<button class="px-4 py-2 text-gray-700 font-medium rounded-lg
               hover:bg-gray-100 focus:outline-none focus:ring-2
               focus:ring-primary-500 focus:ring-offset-2
               transition-colors duration-200">
  Ghost Action
</button>
```

### Input Fields

#### Text Input

```html
<div class="space-y-1">
  <label for="email" class="block text-sm font-medium text-gray-700">
    Email
  </label>
  <input
    type="email"
    id="email"
    class="w-full px-3 py-2 border border-gray-300 rounded-lg
           focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent
           disabled:bg-gray-100 disabled:cursor-not-allowed"
    placeholder="you@example.com"
  />
  <p class="text-sm text-gray-500">
    We'll never share your email.
  </p>
</div>
```

#### Input with Error

```html
<div class="space-y-1">
  <label for="password" class="block text-sm font-medium text-gray-700">
    Password
  </label>
  <input
    type="password"
    id="password"
    class="w-full px-3 py-2 border border-error-500 rounded-lg
           focus:outline-none focus:ring-2 focus:ring-error-500 focus:border-transparent"
  />
  <p class="text-sm text-error-600">
    Password is required
  </p>
</div>
```

### Cards

#### Basic Card

```html
<div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
  <h3 class="text-lg font-semibold text-gray-900 mb-2">
    Card Title
  </h3>
  <p class="text-sm text-gray-600">
    Card content goes here
  </p>
</div>
```

#### Hover Card

```html
<div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6
            hover:shadow-md transition-shadow duration-200 cursor-pointer">
  <!-- Card content -->
</div>
```

### Badges

```html
<!-- Success badge -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full
             text-xs font-medium bg-success-100 text-success-800">
  Active
</span>

<!-- Warning badge -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full
             text-xs font-medium bg-warning-100 text-warning-800">
  Pending
</span>

<!-- Error badge -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full
             text-xs font-medium bg-error-100 text-error-800">
  Inactive
</span>

<!-- Info badge -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full
             text-xs font-medium bg-info-100 text-info-800">
  New
</span>
```

### Alerts

```html
<!-- Success alert -->
<div class="rounded-md bg-success-50 p-4 border border-success-200">
  <div class="flex">
    <div class="flex-shrink-0">
      <svg class="h-5 w-5 text-success-400" />
    </div>
    <div class="ml-3">
      <h3 class="text-sm font-medium text-success-800">
        Success!
      </h3>
      <p class="mt-1 text-sm text-success-700">
        Your changes have been saved.
      </p>
    </div>
  </div>
</div>

<!-- Error alert -->
<div class="rounded-md bg-error-50 p-4 border border-error-200">
  <div class="flex">
    <div class="flex-shrink-0">
      <svg class="h-5 w-5 text-error-400" />
    </div>
    <div class="ml-3">
      <h3 class="text-sm font-medium text-error-800">
        Error
      </h3>
      <p class="mt-1 text-sm text-error-700">
        Something went wrong. Please try again.
      </p>
    </div>
  </div>
</div>
```

### Tables

```html
<div class="overflow-x-auto">
  <table class="min-w-full divide-y divide-gray-200">
    <thead class="bg-gray-50">
      <tr>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
          Name
        </th>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
          Status
        </th>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
          Actions
        </th>
      </tr>
    </thead>
    <tbody class="bg-white divide-y divide-gray-200">
      <tr class="hover:bg-gray-50">
        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
          John Doe
        </td>
        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
          <span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-success-100 text-success-800">
            Active
          </span>
        </td>
        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
          <button class="text-primary-600 hover:text-primary-900">Edit</button>
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

---

## Icons

### Icon Library: Lucide React

```bash
npm install lucide-react
```

### Icon Usage

```tsx
import { Home, User, Settings, ShoppingCart } from 'lucide-react';

// Default size (24x24)
<Home />

// Custom size
<User size={16} />

// Custom color
<Settings className="text-primary-600" />

// With stroke width
<ShoppingCart size={20} strokeWidth={1.5} />
```

### Common Icons

```tsx
// Navigation
import { Home, LayoutDashboard, Package, TrendingUp, Settings } from 'lucide-react';

// Actions
import { Plus, Edit, Trash, Save, Download, Upload } from 'lucide-react';

// Status
import { CheckCircle, AlertCircle, XCircle, Info } from 'lucide-react';

// UI
import { ChevronDown, ChevronRight, Search, Filter, Menu, X } from 'lucide-react';
```

---

## Shadows & Effects

### Box Shadows

```css
boxShadow: {
  sm:   '0 1px 2px 0 rgb(0 0 0 / 0.05)',
  DEFAULT: '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
  md:   '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
  lg:   '0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)',
  xl:   '0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)',
}
```

**Usage**:
- Cards: `shadow-sm`
- Dropdowns: `shadow-md`
- Modals: `shadow-lg`
- Popovers: `shadow-xl`

---

## Animations

### Transitions

```css
transition: {
  none: 'none',
  all: 'all 150ms cubic-bezier(0.4, 0, 0.2, 1)',
  colors: 'color, background-color, border-color 200ms',
  opacity: 'opacity 200ms',
  transform: 'transform 200ms',
}
```

### Keyframe Animations

```css
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from {
    transform: translateY(-10px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.animate-fadeIn {
  animation: fadeIn 200ms ease-in;
}

.animate-slideIn {
  animation: slideIn 200ms ease-out;
}
```

---

## Responsive Design

### Breakpoints

```css
screens: {
  sm: '640px',   /* Mobile landscape */
  md: '768px',   /* Tablet portrait */
  lg: '1024px',  /* Tablet landscape / Small desktop */
  xl: '1280px',  /* Desktop */
  '2xl': '1536px', /* Large desktop */
}
```

### Responsive Utilities

```html
<!-- Hide on mobile, show on desktop -->
<div class="hidden md:block">
  Desktop only content
</div>

<!-- Stack on mobile, row on desktop -->
<div class="flex flex-col md:flex-row gap-4">
  <div>Column 1</div>
  <div>Column 2</div>
</div>

<!-- Responsive text sizes -->
<h1 class="text-2xl md:text-3xl lg:text-4xl">
  Responsive heading
</h1>

<!-- Responsive padding -->
<div class="p-4 md:p-6 lg:p-8">
  Responsive padding
</div>
```

---

## Dark Mode

### Implementation

```tsx
// Use ThemeContext from FRONTEND_ARCHITECTURE.md

<html class="dark">
  <!-- Dark mode active -->
</html>
```

### Dark Mode Colors

```html
<!-- Text -->
<p class="text-gray-900 dark:text-gray-100">
  Dark mode text
</p>

<!-- Background -->
<div class="bg-white dark:bg-gray-800">
  Dark mode background
</div>

<!-- Border -->
<div class="border-gray-200 dark:border-gray-700">
  Dark mode border
</div>
```

---

## Accessibility

### Focus States

```html
<button class="focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2">
  Accessible button
</button>
```

### ARIA Labels

```html
<button aria-label="Close dialog">
  <X size={20} />
</button>

<input
  type="search"
  aria-label="Search products"
  placeholder="Search..."
/>
```

### Keyboard Navigation

- All interactive elements are keyboard accessible
- Tab order follows visual order
- Focus indicators are always visible
- Skip links for main content

---

**Document Version**: 1.0
**Last Updated**: January 2025
**Maintained By**: Design Team
