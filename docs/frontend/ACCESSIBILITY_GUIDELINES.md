# Accessibility Guidelines

**Version**: 1.0
**Last Updated**: January 2025
**Standard**: WCAG 2.1 Level AA

---

## Accessibility Principles

1. **Perceivable**: Information must be presentable to users in ways they can perceive
2. **Operable**: UI components must be operable
3. **Understandable**: Information and UI operation must be understandable
4. **Robust**: Content must be robust enough to be interpreted by assistive technologies

---

## Color & Contrast

### Contrast Ratios (WCAG AA)

- **Normal text**: Minimum 4.5:1
- **Large text** (18pt+): Minimum 3:1
- **UI components**: Minimum 3:1

### Color Usage

❌ **Don't**:
- Use color alone to convey information
- Use red/green only for error/success

✅ **Do**:
- Use color + icons + text labels
- Provide patterns or shapes alongside color

```tsx
// Bad
<div className="bg-red-500">Error</div>

// Good
<div className="bg-error-500 flex items-center gap-2">
  <XCircle className="h-5 w-5" />
  <span>Error occurred</span>
</div>
```

---

## Keyboard Navigation

### Focus Management

All interactive elements must be keyboard accessible:

```tsx
<button
  className="focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
  tabIndex={0}
>
  Accessible Button
</button>
```

### Tab Order

- Tab order follows visual order
- Skip links for main content
- Modal dialogs trap focus

```tsx
// Skip link
<a href="#main-content" className="sr-only focus:not-sr-only">
  Skip to main content
</a>

<main id="main-content">
  {/* Page content */}
</main>
```

### Keyboard Shortcuts

- `Tab`: Move forward
- `Shift+Tab`: Move backward
- `Enter/Space`: Activate buttons
- `Esc`: Close modals/dialogs
- `Arrow keys`: Navigate menus/lists

---

## Semantic HTML

Use appropriate HTML elements:

```tsx
// ✅ Good
<nav aria-label="Main navigation">
  <ul>
    <li><a href="/dashboard">Dashboard</a></li>
  </ul>
</nav>

<main>
  <h1>Page Title</h1>
  <article>
    <h2>Section Title</h2>
  </article>
</main>

// ❌ Bad
<div className="nav">
  <div className="link">Dashboard</div>
</div>
```

---

## ARIA Attributes

### Labels

```tsx
// Button with icon only
<button aria-label="Close dialog">
  <X className="h-5 w-5" />
</button>

// Input with label
<label htmlFor="email">Email Address</label>
<input id="email" type="email" aria-required="true" />

// Describedby
<input
  id="password"
  type="password"
  aria-describedby="password-help"
/>
<span id="password-help">
  Password must be at least 8 characters
</span>
```

### Live Regions

```tsx
// Success message
<div
  role="status"
  aria-live="polite"
  aria-atomic="true"
>
  Changes saved successfully
</div>

// Error message
<div
  role="alert"
  aria-live="assertive"
  aria-atomic="true"
>
  An error occurred
</div>
```

### States

```tsx
// Expanded/collapsed
<button
  aria-expanded={isOpen}
  aria-controls="menu"
  onClick={toggle}
>
  Menu
</button>

// Disabled
<button disabled aria-disabled="true">
  Submit
</button>

// Selected
<button aria-pressed={isSelected}>
  Toggle
</button>
```

---

## Forms

### Form Labels

```tsx
// ✅ Always use labels
<div>
  <label htmlFor="username">Username</label>
  <input id="username" type="text" />
</div>

// For screen readers only
<label htmlFor="search" className="sr-only">
  Search products
</label>
<input id="search" type="search" />
```

### Error Messages

```tsx
<div>
  <label htmlFor="email">Email</label>
  <input
    id="email"
    type="email"
    aria-invalid={hasError}
    aria-describedby={hasError ? "email-error" : undefined}
  />
  {hasError && (
    <span id="email-error" role="alert">
      Email is required
    </span>
  )}
</div>
```

### Required Fields

```tsx
<label htmlFor="name">
  Name <span aria-label="required">*</span>
</label>
<input id="name" type="text" required aria-required="true" />
```

---

## Images & Icons

### Alternative Text

```tsx
// Decorative image
<img src="decoration.png" alt="" role="presentation" />

// Informative image
<img src="chart.png" alt="Revenue trend showing 20% growth" />

// Icon with text
<button>
  <Save className="h-4 w-4" aria-hidden="true" />
  <span>Save</span>
</button>

// Icon only
<button aria-label="Save changes">
  <Save className="h-4 w-4" />
</button>
```

---

## Tables

```tsx
<table>
  <caption>Sales by Product</caption>
  <thead>
    <tr>
      <th scope="col">Product</th>
      <th scope="col">Sales</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th scope="row">Product A</th>
      <td>$1,000</td>
    </tr>
  </tbody>
</table>
```

---

## Modals & Dialogs

```tsx
import { Dialog } from '@/components/ui/dialog';

<Dialog
  open={isOpen}
  onOpenChange={setIsOpen}
  aria-labelledby="dialog-title"
  aria-describedby="dialog-description"
>
  <DialogContent>
    <DialogTitle id="dialog-title">
      Confirm Action
    </DialogTitle>
    <DialogDescription id="dialog-description">
      Are you sure you want to proceed?
    </DialogDescription>
    {/* Dialog content */}
  </DialogContent>
</Dialog>
```

---

## Testing Checklist

### Automated Testing

- [ ] Run Lighthouse accessibility audit
- [ ] Use axe DevTools browser extension
- [ ] Check with pa11y CI tool

### Manual Testing

- [ ] Navigate entire app with keyboard only
- [ ] Test with screen reader (NVDA, JAWS, VoiceOver)
- [ ] Verify color contrast meets WCAG AA
- [ ] Test with browser zoom at 200%
- [ ] Test with reduced motion enabled
- [ ] Verify focus indicators are visible
- [ ] Check all form labels are associated
- [ ] Ensure all interactive elements are focusable

### Screen Reader Testing

**Test with**:
- NVDA (Windows) - Free
- JAWS (Windows) - Commercial
- VoiceOver (Mac) - Built-in
- TalkBack (Android) - Built-in

---

## Common Patterns

### Loading States

```tsx
<div role="status" aria-live="polite">
  {isLoading ? (
    <div>
      <LoadingSpinner aria-hidden="true" />
      <span className="sr-only">Loading...</span>
    </div>
  ) : (
    <div>Content loaded</div>
  )}
</div>
```

### Empty States

```tsx
<div role="status">
  <p>No items found</p>
</div>
```

### Pagination

```tsx
<nav aria-label="Pagination">
  <ul className="flex gap-2">
    <li>
      <button aria-label="Previous page" disabled={isFirstPage}>
        Previous
      </button>
    </li>
    <li>
      <button aria-label="Page 1" aria-current="page">
        1
      </button>
    </li>
    <li>
      <button aria-label="Next page" disabled={isLastPage}>
        Next
      </button>
    </li>
  </ul>
</nav>
```

---

## Resources

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [MDN Accessibility](https://developer.mozilla.org/en-US/docs/Web/Accessibility)
- [WebAIM Resources](https://webaim.org/resources/)
- [A11y Project Checklist](https://www.a11yproject.com/checklist/)

---

**Document Version**: 1.0
**Last Updated**: January 2025
