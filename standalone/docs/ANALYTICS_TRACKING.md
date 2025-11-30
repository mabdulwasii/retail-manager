# Shop Manager - Analytics & Tracking Guide

Comprehensive guide for tracking metrics, analyzing user behavior, and measuring success.

## Table of Contents

1. [Analytics Philosophy](#analytics-philosophy)
2. [Key Metrics to Track](#key-metrics-to-track)
3. [Google Analytics Setup](#google-analytics-setup)
4. [Product Analytics](#product-analytics)
5. [Download Tracking](#download-tracking)
6. [User Behavior Analytics](#user-behavior-analytics)
7. [Marketing Attribution](#marketing-attribution)
8. [Custom Dashboards](#custom-dashboards)
9. [Privacy & Compliance](#privacy--compliance)
10. [Reporting](#reporting)

---

## Analytics Philosophy

**Principles**:
- **Privacy First**: Never track personal data without consent
- **Actionable**: Only track metrics that inform decisions
- **Transparent**: Users should know what's being tracked
- **Compliant**: Follow GDPR, CCPA, and local regulations
- **Ethical**: No dark patterns or manipulative tracking

**What We Track**:
- Website traffic and behavior
- Download statistics
- Installation success rates
- Feature usage (anonymized)
- Error rates and crashes

**What We DON'T Track**:
- Personal customer data
- Sales transactions
- Individual user identities (without consent)
- Sensitive business information
- Location data (beyond country-level)

---

## Key Metrics to Track

### 1. Acquisition Metrics

**Website Traffic**:
- Unique visitors (monthly, weekly, daily)
- Page views
- Traffic sources (organic, social, direct, referral)
- Geographic distribution
- Device breakdown (desktop vs mobile)

**Conversion Funnel**:
```
100 visitors
  ↓ (30%)
30 reach download page
  ↓ (40%)
12 start download
  ↓ (80%)
10 complete download
  ↓ (60%)
6 successful installations
  ↓ (50%)
3 active users (7-day)
```

**Key Questions**:
- Where are users coming from?
- Which channels convert best?
- Where do users drop off?

---

### 2. Activation Metrics

**Installation Success**:
- Downloads started
- Downloads completed
- Installations attempted
- Installations successful
- Setup wizard completion rate

**Time to Value**:
- Time from download to first login
- Time from login to first sale
- Setup wizard completion time

**Target**:
- Installation success rate: >90%
- Setup completion rate: >80%
- Time to first sale: <30 minutes

---

### 3. Engagement Metrics

**Usage Frequency**:
- Daily Active Users (DAU)
- Weekly Active Users (WAU)
- Monthly Active Users (MAU)
- DAU/MAU ratio (stickiness)

**Feature Adoption**:
- % users using POS
- % users using inventory management
- % users using reports
- % users with >1 shop
- % users with >1 user account

**Session Metrics**:
- Average session duration
- Sessions per user
- Time between sessions

---

### 4. Retention Metrics

**Cohort Analysis**:
```
Week 0: 100 new users
Week 1: 70 users active (70% retention)
Week 2: 55 users active (55% retention)
Week 4: 45 users active (45% retention)
Week 8: 40 users active (40% retention)
```

**Churn Rate**:
- Weekly churn: Users who don't return in 7 days
- Monthly churn: Users who don't return in 30 days

**Target**:
- 1-week retention: >60%
- 1-month retention: >40%
- 3-month retention: >30%

---

### 5. Product Quality Metrics

**Errors & Crashes**:
- Error rate (errors per session)
- Crash rate (% sessions with crashes)
- Top error messages
- Affected users

**Performance**:
- Page load times (p50, p95, p99)
- API response times
- Database query performance
- Time to interactive

**Target**:
- Error rate: <1%
- Crash rate: <0.5%
- Dashboard load: <2s (p95)
- API response: <500ms (p95)

---

### 6. Business Metrics

**Growth**:
- New users (weekly, monthly)
- Growth rate (% week-over-week)
- Cumulative users

**Distribution**:
- Downloads by platform (Windows/macOS/Linux)
- Downloads by channel (website, GitHub, app stores)
- Version distribution

**Support**:
- Support tickets (volume, response time)
- GitHub issues (open, closed, time to close)
- Community activity (Discord messages, questions)

---

## Google Analytics Setup

### 1. Create GA4 Property

1. Go to https://analytics.google.com
2. Admin → Create Property
3. Property name: "Shop Manager"
4. Time zone: Your timezone
5. Currency: USD
6. Enable Google Signals: Yes (for demographics)
7. Create

### 2. Set Up Data Stream

1. Admin → Data Streams → Add stream
2. Platform: Web
3. Website URL: https://shopmanager.com
4. Stream name: "Shop Manager Website"
5. Enhanced measurement: ON
   - Page views ✓
   - Scrolls ✓
   - Outbound clicks ✓
   - Site search ✓
   - Video engagement ✓
   - File downloads ✓

### 3. Install Tracking Code

Add to `standalone/marketing/index.html`:

```html
<head>
  <!-- Google tag (gtag.js) -->
  <script async src="https://www.googletagmanager.com/gtag/js?id=G-XXXXXXXXXX"></script>
  <script>
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());

    gtag('config', 'G-XXXXXXXXXX', {
      'send_page_view': true,
      'anonymize_ip': true  // GDPR compliance
    });
  </script>
</head>
```

### 4. Configure Custom Events

Edit `standalone/marketing/script.js`:

```javascript
// Track download clicks
function trackDownload(platform, version) {
  gtag('event', 'download_started', {
    'event_category': 'Downloads',
    'event_label': platform,
    'value': version,
    'platform': platform,
    'version': version
  });
}

// Track video plays
function trackVideoPlay(videoName) {
  gtag('event', 'video_start', {
    'event_category': 'Engagement',
    'event_label': videoName,
    'video_title': videoName
  });
}

// Track button clicks
function trackButtonClick(buttonName) {
  gtag('event', 'button_click', {
    'event_category': 'Engagement',
    'event_label': buttonName,
    'button_name': buttonName
  });
}

// Track form submissions
function trackFormSubmit(formName) {
  gtag('event', 'form_submit', {
    'event_category': 'Conversion',
    'event_label': formName,
    'form_name': formName
  });
}

// Track external links
document.querySelectorAll('a[href^="http"]').forEach(link => {
  link.addEventListener('click', function() {
    gtag('event', 'click', {
      'event_category': 'Outbound Link',
      'event_label': this.href
    });
  });
});

// Update download buttons to track
document.querySelectorAll('.download-card .btn-primary').forEach((button, index) => {
  button.addEventListener('click', function(e) {
    const platform = this.closest('.download-card').querySelector('h3').textContent;
    const version = '1.0.0'; // Update dynamically
    trackDownload(platform, version);
  });
});
```

### 5. Set Up Conversions

In Google Analytics:
1. Admin → Events
2. Mark these as conversions:
   - `download_started`
   - `download_completed`
   - `form_submit`
   - `purchase` (if selling)

---

## Product Analytics

### Telemetry in Desktop App

**Opt-in Telemetry** (with user consent):

Create `standalone/electron-app/src/telemetry.js`:

```javascript
const { ipcMain } = require('electron');
const Store = require('electron-store');
const store = new Store();

class Telemetry {
  constructor() {
    this.enabled = store.get('telemetry.enabled', false);
    this.userId = store.get('telemetry.userId', this.generateAnonymousId());
  }

  generateAnonymousId() {
    return 'anon_' + Math.random().toString(36).substr(2, 9);
  }

  async trackEvent(eventName, properties = {}) {
    if (!this.enabled) return;

    const event = {
      event: eventName,
      properties: {
        ...properties,
        userId: this.userId,
        platform: process.platform,
        version: require('../../package.json').version,
        timestamp: new Date().toISOString()
      }
    };

    // Send to analytics endpoint
    try {
      await fetch('https://analytics.shopmanager.com/event', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(event)
      });
    } catch (error) {
      console.error('Telemetry error:', error);
    }
  }

  enable() {
    this.enabled = true;
    store.set('telemetry.enabled', true);
    this.trackEvent('telemetry_enabled');
  }

  disable() {
    this.enabled = false;
    store.set('telemetry.enabled', false);
  }
}

module.exports = new Telemetry();
```

**Consent Dialog** in setup wizard:

```javascript
// In setup wizard
async function showTelemetryConsent() {
  const result = await dialog.showMessageBox({
    type: 'question',
    title: 'Help Improve Shop Manager',
    message: 'Would you like to send anonymous usage data to help us improve Shop Manager?',
    detail: 'We collect:\n' +
            '- Feature usage (what you click)\n' +
            '- Error reports\n' +
            '- Performance metrics\n\n' +
            'We DO NOT collect:\n' +
            '- Personal data\n' +
            '- Sales information\n' +
            '- Customer data\n\n' +
            'You can change this anytime in Settings.',
    buttons: ['Yes, help improve', 'No thanks'],
    defaultId: 0,
    cancelId: 1
  });

  if (result.response === 0) {
    telemetry.enable();
  }
}
```

**Events to Track** (with consent):

```javascript
// Application lifecycle
telemetry.trackEvent('app_started');
telemetry.trackEvent('app_closed', { sessionDuration: 3600 });

// Installation
telemetry.trackEvent('installation_started');
telemetry.trackEvent('installation_completed', { duration: 120 });
telemetry.trackEvent('installation_failed', { error: 'docker_not_running' });

// Setup wizard
telemetry.trackEvent('setup_wizard_started');
telemetry.trackEvent('setup_wizard_completed', { duration: 300 });
telemetry.trackEvent('setup_wizard_abandoned', { step: 2 });

// Feature usage
telemetry.trackEvent('feature_used', { feature: 'pos' });
telemetry.trackEvent('feature_used', { feature: 'inventory' });
telemetry.trackEvent('feature_used', { feature: 'reports' });

// Errors
telemetry.trackEvent('error_occurred', {
  error: 'service_start_failed',
  service: 'backend',
  message: 'Port already in use'
});

// Performance
telemetry.trackEvent('performance', {
  metric: 'dashboard_load_time',
  value: 2.5,
  unit: 'seconds'
});
```

---

## Download Tracking

### GitHub Releases API

Track downloads from GitHub:

```javascript
// scripts/track-downloads.js
const fetch = require('node-fetch');

async function getDownloadStats() {
  const response = await fetch(
    'https://api.github.com/repos/yourorg/shop-manager/releases'
  );
  const releases = await response.json();

  const stats = releases.map(release => ({
    version: release.tag_name,
    published: release.published_at,
    downloads: release.assets.reduce((total, asset) => {
      return total + asset.download_count;
    }, 0),
    assets: release.assets.map(asset => ({
      name: asset.name,
      downloads: asset.download_count,
      size: asset.size
    }))
  }));

  return stats;
}

// Run daily and store in database
getDownloadStats().then(stats => {
  console.log(JSON.stringify(stats, null, 2));
});
```

**Output Example**:
```json
[
  {
    "version": "v1.0.0",
    "published": "2024-01-10T00:00:00Z",
    "downloads": 1523,
    "assets": [
      {
        "name": "Shop-Manager-Setup-1.0.0.exe",
        "downloads": 687,
        "size": 141557760
      },
      {
        "name": "Shop-Manager-1.0.0.dmg",
        "downloads": 523,
        "size": 134217728
      },
      {
        "name": "Shop-Manager-1.0.0.AppImage",
        "downloads": 313,
        "size": 148897792
      }
    ]
  }
]
```

### Custom Download Endpoint

Create download proxy to track:

```javascript
// On your website (Node.js/Express)
app.get('/download/:platform/:version', async (req, res) => {
  const { platform, version } = req.params;

  // Track download
  await analytics.track({
    event: 'download_started',
    properties: {
      platform,
      version,
      userAgent: req.headers['user-agent'],
      referrer: req.headers['referer'],
      ip: req.ip
    }
  });

  // Redirect to actual file
  const fileUrls = {
    'windows': `https://github.com/yourorg/shop-manager/releases/download/${version}/Shop-Manager-Setup-${version}.exe`,
    'macos': `https://github.com/yourorg/shop-manager/releases/download/${version}/Shop-Manager-${version}.dmg`,
    'linux': `https://github.com/yourorg/shop-manager/releases/download/${version}/Shop-Manager-${version}.AppImage`
  };

  res.redirect(fileUrls[platform]);
});
```

Update landing page links:
```html
<!-- Instead of direct GitHub link -->
<a href="https://shopmanager.com/download/windows/1.0.0">Download for Windows</a>
```

---

## User Behavior Analytics

### Heatmaps & Session Recording

**Tools**:
- **Hotjar** ($39/month): Heatmaps, recordings, surveys
- **Microsoft Clarity** (Free): Heatmaps and session recordings
- **FullStory** ($199/month): Advanced session replay

**Setup (Microsoft Clarity - Free)**:

1. Sign up at https://clarity.microsoft.com
2. Create project
3. Copy tracking code

Add to `index.html`:
```html
<script type="text/javascript">
  (function(c,l,a,r,i,t,y){
    c[a]=c[a]||function(){(c[a].q=c[a].q||[]).push(arguments)};
    t=l.createElement(r);t.async=1;t.src="https://www.clarity.ms/tag/"+i;
    y=l.getElementsByTagName(r)[0];y.parentNode.insertBefore(t,y);
  })(window, document, "clarity", "script", "YOUR_PROJECT_ID");
</script>
```

**Use Cases**:
- See where users click most
- Identify confusing UI elements
- Watch users struggle with forms
- Find mobile usability issues

---

## Marketing Attribution

### UTM Parameters

Track campaign effectiveness with UTM codes:

**UTM Structure**:
```
https://shopmanager.com?utm_source=twitter&utm_medium=social&utm_campaign=launch
```

**Parameters**:
- `utm_source`: Where (twitter, facebook, google, email)
- `utm_medium`: How (social, cpc, email, organic)
- `utm_campaign`: Why (launch, black-friday, feature-update)
- `utm_content`: Which (ad-variant-a, button-blue)
- `utm_term`: Keyword (for paid search)

**Examples**:

**Twitter Launch Post**:
```
https://shopmanager.com?utm_source=twitter&utm_medium=social&utm_campaign=v1_launch&utm_content=announcement_tweet
```

**Facebook Ad**:
```
https://shopmanager.com?utm_source=facebook&utm_medium=cpc&utm_campaign=retargeting&utm_content=video_ad
```

**Email Newsletter**:
```
https://shopmanager.com?utm_source=newsletter&utm_medium=email&utm_campaign=monthly_update&utm_content=header_cta
```

**Product Hunt**:
```
https://shopmanager.com?utm_source=producthunt&utm_medium=referral&utm_campaign=launch_day
```

### Track in Google Analytics

1. Acquisition → Traffic acquisition
2. Add secondary dimension: Session campaign
3. See which campaigns drive most traffic/conversions

---

## Custom Dashboards

### Google Analytics Dashboard

Create dashboard for weekly review:

**Widget 1: Traffic Overview**
- Metric: Users, Sessions, Bounce Rate
- Timeframe: Last 30 days vs previous 30 days

**Widget 2: Top Pages**
- Dimension: Page path
- Metric: Page views, Avg. time on page
- Top 10 pages

**Widget 3: Traffic Sources**
- Dimension: Source/Medium
- Metric: Users, Conversion rate
- Pie chart

**Widget 4: Downloads by Platform**
- Event: download_started
- Dimension: Platform (Windows/macOS/Linux)
- Bar chart

**Widget 5: Conversion Funnel**
- Step 1: Landing page view
- Step 2: Download page view
- Step 3: Download started
- Step 4: (If trackable) Installation completed

**Widget 6: Geographic Distribution**
- Dimension: Country
- Metric: Users
- Map view

### Custom Dashboard (If Building Your Own)

**Tech Stack**:
- **Frontend**: React, Recharts, Tailwind CSS
- **Backend**: Node.js, Express, PostgreSQL
- **Visualization**: Chart.js, D3.js

**Sample Dashboard**:

```javascript
// Dashboard component
import { LineChart, BarChart, PieChart } from 'recharts';

function AnalyticsDashboard() {
  const [stats, setStats] = useState({});

  useEffect(() => {
    fetch('/api/analytics/summary')
      .then(res => res.json())
      .then(data => setStats(data));
  }, []);

  return (
    <div className="dashboard">
      <div className="grid grid-cols-4 gap-4">
        {/* KPI Cards */}
        <Card title="Total Downloads" value={stats.totalDownloads} change="+15%" />
        <Card title="Active Users (30d)" value={stats.activeUsers} change="+8%" />
        <Card title="Avg. Session (min)" value={stats.avgSession} change="-2%" />
        <Card title="Crash Rate" value={stats.crashRate} change="-0.2%" />
      </div>

      <div className="grid grid-cols-2 gap-4 mt-4">
        {/* Charts */}
        <LineChart data={stats.dailyDownloads} title="Downloads Over Time" />
        <PieChart data={stats.platformDistribution} title="Platform Share" />
        <BarChart data={stats.topFeatures} title="Feature Usage" />
        <LineChart data={stats.retentionCohort} title="Retention Curve" />
      </div>
    </div>
  );
}
```

---

## Privacy & Compliance

### GDPR Compliance

**Requirements**:
1. **Consent**: Get explicit consent before tracking
2. **Transparency**: Clear privacy policy
3. **Access**: Users can request their data
4. **Deletion**: Users can delete their data
5. **Portability**: Users can export their data

**Implementation**:

**Cookie Consent Banner**:

```html
<div id="cookie-consent" class="cookie-banner">
  <p>
    We use cookies to improve your experience.
    <a href="/privacy">Learn more</a>
  </p>
  <button onclick="acceptCookies()">Accept</button>
  <button onclick="rejectCookies()">Reject</button>
</div>

<script>
function acceptCookies() {
  localStorage.setItem('cookieConsent', 'accepted');
  document.getElementById('cookie-consent').style.display = 'none';
  initializeAnalytics();
}

function rejectCookies() {
  localStorage.setItem('cookieConsent', 'rejected');
  document.getElementById('cookie-consent').style.display = 'none';
}

// Only initialize analytics if consent given
if (localStorage.getItem('cookieConsent') === 'accepted') {
  initializeAnalytics();
}
</script>
```

**Privacy Policy** (key sections):

```markdown
# Privacy Policy

## What We Collect

### Website Analytics
- Pages visited
- Time spent on site
- Geographic location (country-level)
- Device type and browser

### Product Telemetry (Opt-in Only)
- Feature usage
- Error reports
- Performance metrics

We DO NOT collect:
- Personal information
- Sales data
- Customer data
- Precise location

## How We Use Data

- Improve product
- Fix bugs
- Understand feature usage
- Marketing attribution

## Data Retention

- Analytics: 26 months
- Error logs: 90 days
- Telemetry: Aggregated and anonymized after 30 days

## Your Rights

- Access your data
- Delete your data
- Opt-out of tracking
- Export your data

Contact: privacy@shopmanager.com

## Third-Party Services

- Google Analytics (analytics)
- Microsoft Clarity (heatmaps)
- Cloudflare (CDN)

Last updated: January 15, 2024
```

### Do Not Track

Respect Do Not Track (DNT) header:

```javascript
if (navigator.doNotTrack === '1') {
  console.log('DNT enabled, disabling analytics');
} else {
  initializeAnalytics();
}
```

---

## Reporting

### Weekly Analytics Report

```markdown
# Shop Manager Analytics - Week of [Date]

## Key Metrics

| Metric | This Week | Last Week | Change |
|--------|-----------|-----------|--------|
| Website Visitors | 2,345 | 2,100 | +12% |
| Downloads | 156 | 142 | +10% |
| New Installations | 128 | 120 | +7% |
| Active Users (7d) | 89 | 85 | +5% |

## Traffic Sources

1. Organic Search: 45% (1,055 visitors)
2. Direct: 25% (586 visitors)
3. Social Media: 20% (469 visitors)
   - Twitter: 60%
   - LinkedIn: 25%
   - Reddit: 15%
4. Referral: 10% (235 visitors)
   - Product Hunt: 120
   - GitHub: 85
   - Dev.to: 30

## Top Performing Content

1. Homepage: 2,345 views
2. Download page: 856 views (36% conversion from homepage)
3. Documentation: 432 views
4. Pricing: 298 views

## Downloads by Platform

- Windows: 65 (42%)
- macOS: 52 (33%)
- Linux: 39 (25%)

## User Behavior

- Avg. session duration: 3:24
- Bounce rate: 42% (target: <50%)
- Pages per session: 2.8

## Conversions

- Downloads started: 156
- Est. installations: 128 (82% completion rate)
- Setup wizard completion: 98 (77%)

## Issues & Alerts

- ⚠️ Bounce rate increased 5% on mobile (investigate)
- ✅ Download completion rate improved to 82%
- 📈 Reddit traffic spike (+150%) after r/selfhosted post

## Action Items

- [ ] Optimize mobile landing page (high bounce rate)
- [ ] Create tutorial video for setup wizard (23% abandon)
- [ ] Increase Reddit engagement (high-quality traffic)
- [ ] A/B test new CTA buttons

## Next Week Goals

- 2,500 visitors (+7%)
- 170 downloads (+9%)
- <40% bounce rate
```

---

## Tools Comparison

### Analytics Platforms

| Tool | Cost | Best For | Privacy |
|------|------|----------|---------|
| **Google Analytics 4** | Free | General website analytics | Good (anonymize IP) |
| **Plausible** | $9/mo | Privacy-focused, simple | Excellent (GDPR compliant) |
| **Fathom** | $14/mo | Privacy-first, no cookies | Excellent (GDPR compliant) |
| **Mixpanel** | Free tier | Product analytics, funnels | Good |
| **Amplitude** | Free tier | Advanced product analytics | Good |
| **Matomo** | Free (self-hosted) | Full control, privacy | Excellent (self-hosted) |

**Recommendation**:
- **Start**: Google Analytics 4 (free, powerful)
- **Privacy-conscious**: Plausible or Fathom
- **Product analytics**: Mixpanel or Amplitude
- **Full control**: Matomo (self-hosted)

---

## Next Steps

1. **Set up GA4**: Install tracking code on website
2. **Configure events**: Track downloads, videos, forms
3. **Create dashboard**: Weekly metrics dashboard
4. **Privacy policy**: Write and publish
5. **Opt-in telemetry**: Add to Electron app (with consent)
6. **Weekly reviews**: Schedule analytics review meetings

---

## Resources

**Learning**:
- Google Analytics Academy: https://analytics.google.com/analytics/academy/
- Mixpanel Product Analytics Guide: https://mixpanel.com/content/guide/
- Privacy-first analytics: https://plausible.io/data-policy

**Tools**:
- UTM Builder: https://ga-dev-tools.google/campaign-url-builder/
- Regex tester for GA: https://regex101.com
- Dashboard templates: https://analytics.google.com/analytics/gallery/

**Privacy**:
- GDPR checklist: https://gdpr.eu/checklist/
- Cookie consent tools: https://www.cookiebot.com
- Privacy policy generator: https://www.termsfeed.com/privacy-policy-generator/

---

## Contact

For analytics questions:
- **Email**: analytics@shopmanager.com
- **Slack**: #analytics channel
- **Lead**: data@shopmanager.com
