# Shop Manager - Customer Support Guide

Comprehensive guide for providing excellent customer support for Shop Manager users.

## Table of Contents

1. [Support Philosophy](#support-philosophy)
2. [Support Channels](#support-channels)
3. [Response Time Targets](#response-time-targets)
4. [Common Issues & Solutions](#common-issues--solutions)
5. [Troubleshooting Workflows](#troubleshooting-workflows)
6. [Knowledge Base](#knowledge-base)
7. [Support Escalation](#support-escalation)
8. [User Communication Templates](#user-communication-templates)
9. [Support Metrics](#support-metrics)
10. [Community Management](#community-management)

---

## Support Philosophy

**Principles**:
- **User-Centric**: Every interaction adds value to the user
- **Proactive**: Anticipate problems before they're reported
- **Transparent**: Honest about limitations and timelines
- **Educational**: Teach users, don't just fix issues
- **Respectful**: Every user deserves excellent support

**Goals**:
- First response within 24 hours (business days)
- 90% user satisfaction rating
- Resolve 80% of issues without escalation
- Build self-service resources to empower users

---

## Support Channels

### 1. Email Support

**Primary**: support@shopmanager.com

**Best for**:
- Complex issues requiring detailed explanation
- Sensitive information (account issues, bugs with data)
- Non-urgent questions
- Feature requests

**Setup**:
```
Use email management tool:
- Google Workspace (gmail)
- Zendesk
- Freshdesk
- Help Scout

Organize with labels:
- bug
- feature-request
- installation
- configuration
- documentation
- billing (if applicable)
```

**SLA**:
- First response: Within 24 hours
- Resolution: Within 5 business days (depending on complexity)

---

### 2. GitHub Issues

**URL**: https://github.com/yourorg/shop-manager/issues

**Best for**:
- Bug reports
- Feature requests
- Technical discussions
- Documentation improvements

**Issue Templates**:

`.github/ISSUE_TEMPLATE/bug_report.yml`:
```yaml
name: Bug Report
description: Report a bug in Shop Manager
title: "[Bug]: "
labels: ["bug", "triage"]
body:
  - type: markdown
    attributes:
      value: Thanks for taking the time to report this bug!

  - type: dropdown
    id: platform
    attributes:
      label: Platform
      options:
        - Windows
        - macOS
        - Linux (Ubuntu)
        - Linux (Fedora)
        - Linux (Other)
    validations:
      required: true

  - type: input
    id: version
    attributes:
      label: Shop Manager Version
      description: What version are you using?
      placeholder: e.g., 1.0.0
    validations:
      required: true

  - type: textarea
    id: description
    attributes:
      label: Description
      description: A clear description of the bug
    validations:
      required: true

  - type: textarea
    id: steps
    attributes:
      label: Steps to Reproduce
      description: How can we reproduce this?
      value: |
        1. Go to '...'
        2. Click on '...'
        3. See error
    validations:
      required: true

  - type: textarea
    id: expected
    attributes:
      label: Expected Behavior
      description: What should have happened?
    validations:
      required: true

  - type: textarea
    id: logs
    attributes:
      label: Logs
      description: Paste relevant logs (docker compose logs)
      render: shell

  - type: checkboxes
    id: checklist
    attributes:
      label: Pre-submission Checklist
      options:
        - label: I've searched existing issues
          required: true
        - label: I'm using the latest version
          required: true
```

**SLA**:
- Triage: Within 48 hours
- Critical bugs: Fix within 1 week
- High priority: Fix within 2 weeks
- Medium/Low: Planned for next release

---

### 3. Discord Community

**URL**: https://discord.gg/shopmanager

**Channels**:
- #announcements (read-only)
- #general (casual chat)
- #support (help from community)
- #feature-requests
- #showcase (users sharing setups)
- #development (for contributors)

**Best for**:
- Quick questions
- Community help
- Real-time discussions
- Networking with other users

**Moderation**:
- Welcome bot for new members
- Rules channel (code of conduct)
- Moderators to keep discussions respectful
- Archive solved questions to #solved-support

**Response Time**:
- Community-driven (best effort)
- Official team checks 2x daily (morning & evening)

---

### 4. Documentation / FAQ

**URL**: https://docs.shopmanager.com

**Sections**:
1. Getting Started
2. Installation Guide
3. User Manual
4. Troubleshooting
5. FAQ
6. API Documentation
7. Video Tutorials

**Self-Service Goal**: 50% of users should find answers without contacting support

---

### 5. Social Media

**Twitter**: @shopmanager
- Quick updates
- Known issues announcements
- User shoutouts

**LinkedIn**: Company page
- Professional updates
- Case studies
- Feature announcements

**Response Time**: Within 4 hours (business hours)

---

## Response Time Targets

### By Channel

| Channel | First Response | Resolution |
|---------|---------------|------------|
| Email | 24 hours | 5 days |
| GitHub Issues | 48 hours | Varies by severity |
| Discord | 4 hours* | Best effort |
| Twitter/Social | 4 hours | Varies |

*Community-driven, official check 2x daily

### By Severity

| Severity | First Response | Update Frequency | Resolution Target |
|----------|---------------|------------------|-------------------|
| Critical | 2 hours | Every 4 hours | 24 hours |
| High | 8 hours | Daily | 3 days |
| Medium | 24 hours | Every 2 days | 1 week |
| Low | 48 hours | Weekly | 2 weeks |

**Critical Issues**:
- Data loss
- Security breach
- Complete system down
- Cannot install/start

**High Priority**:
- Core feature broken
- Performance severely degraded
- Frequent crashes
- Many users affected

**Medium Priority**:
- Minor feature broken
- Workaround available
- Single user affected
- UI glitches

**Low Priority**:
- Cosmetic issues
- Enhancement requests
- Documentation typos

---

## Common Issues & Solutions

### Installation Issues

#### Issue 1: Docker Not Installed

**Symptoms**:
```
Error: Docker is not installed or not running
```

**Solution**:

**Windows**:
```cmd
1. Download Docker Desktop: https://docker.com/products/docker-desktop
2. Run installer
3. Enable WSL 2 when prompted
4. Restart computer
5. Re-run Shop Manager installer
```

**macOS**:
```bash
1. Download Docker Desktop: https://docker.com/products/docker-desktop
2. Drag to Applications
3. Launch Docker Desktop
4. Wait for "Docker Desktop is running"
5. Re-run Shop Manager installer
```

**Linux**:
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install docker.io docker-compose
sudo systemctl start docker
sudo usermod -aG docker $USER
# Log out and log back in

# Fedora
sudo dnf install docker docker-compose
sudo systemctl start docker
sudo usermod -aG docker $USER
```

---

#### Issue 2: Port Already in Use

**Symptoms**:
```
Error: Port 3001 is already allocated
Error: Port 8080 is already allocated
```

**Solution**:

```bash
# Check what's using the port
# Windows
netstat -ano | findstr :3001

# macOS/Linux
lsof -i :3001

# Option 1: Stop conflicting service
# Option 2: Change port in config.yaml

# Edit config.yaml
ports:
  frontend: 3002  # Changed from 3001
  keycloak: 8081  # Changed from 8080

# Restart
docker compose down
docker compose up -d
```

---

#### Issue 3: SSL Certificate Errors

**Symptoms**:
```
NET::ERR_CERT_AUTHORITY_INVALID
Your connection is not private
```

**Solution**:

```bash
# Regenerate and install certificates
cd standalone
./scripts/install-certs.sh

# Windows: Manual trust
# 1. Open certificates/localhost.crt
# 2. Install Certificate
# 3. Place in "Trusted Root Certification Authorities"

# macOS: Manual trust
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain \
  certificates/localhost.crt

# Linux: Manual trust
sudo cp certificates/localhost.crt /usr/local/share/ca-certificates/
sudo update-ca-certificates
```

---

### Login & Authentication Issues

#### Issue 1: Cannot Login - Invalid Credentials

**Symptoms**:
- "Invalid username or password"
- User exists in Keycloak but can't login

**Solution**:

```bash
# 1. Verify test user credentials in config.yaml
cat config.yaml | grep -A 10 testUsers

# 2. Reset user password in Keycloak
# Open http://localhost:8080
# Login as admin (credentials in config.yaml)
# Users → Select user → Credentials → Reset Password

# 3. Check user attributes
# Users → Select user → Attributes
# Ensure tenantId and shopId are set

# 4. Try default test credentials
Username: admin@shopmanager.com
Password: admin123
```

---

#### Issue 2: Session Timeout Too Aggressive

**Symptoms**:
- Logged out after 5-10 minutes of inactivity

**Solution**:

```yaml
# Edit config.yaml
keycloak:
  session:
    ssoSessionIdleTimeout: 3600  # 1 hour (from 1800)
    ssoSessionMaxLifespan: 36000  # 10 hours (from 18000)

# Regenerate Keycloak realm
cd standalone
python3 scripts/generate-config.py

# Restart Keycloak
docker compose restart keycloak
```

---

### Performance Issues

#### Issue 1: Slow Dashboard Loading

**Symptoms**:
- Dashboard takes 10+ seconds to load
- Browser becomes unresponsive

**Diagnostic Steps**:

```bash
# 1. Check Docker resources
docker stats

# Look for high CPU or memory usage

# 2. Check PostgreSQL connections
docker compose exec postgres psql -U shopmanager -c "SELECT count(*) FROM pg_stat_activity;"

# Should be < 20

# 3. Check backend logs for slow queries
docker compose logs backend | grep "execution time"
```

**Solutions**:

```bash
# Solution 1: Increase Docker resources
# Docker Desktop → Settings → Resources
# RAM: Increase to 8GB (from 4GB)
# CPUs: Increase to 4 (from 2)

# Solution 2: Clear browser cache
# Chrome: Ctrl+Shift+Del → Clear cache

# Solution 3: Restart services
docker compose restart
```

---

#### Issue 2: Database Connection Pool Exhausted

**Symptoms**:
```
Error: Connection pool exhausted
```

**Solution**:

```yaml
# Edit docker-compose.override.yml
services:
  backend:
    environment:
      - SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=50

# Restart
docker compose down
docker compose up -d
```

---

### Data Issues

#### Issue 1: Inventory Count Mismatch

**Symptoms**:
- Inventory shows incorrect stock levels
- Sales completed but inventory not deducted

**Diagnostic**:

```bash
# Check backend logs for FEFO allocation errors
docker compose logs backend | grep -i "inventory"

# Verify database integrity
docker compose exec postgres psql -U shopmanager -d shopmanager -c \
  "SELECT product_id, SUM(current_stock) FROM inventory GROUP BY product_id;"
```

**Solution**:

```bash
# If data is corrupted, restore from backup
docker compose exec postgres pg_restore -U shopmanager -d shopmanager < backup.sql

# Or manually reconcile inventory via UI
# Products → Inventory → Reconcile
```

---

#### Issue 2: Missing Transactions

**Symptoms**:
- Sales made yesterday not showing in reports
- Transaction appears in POS but not in reports

**Diagnostic**:

```bash
# Check Kafka logs (if using event-driven architecture)
docker compose logs kafka | grep -i "sales"

# Check database
docker compose exec postgres psql -U shopmanager -d shopmanager -c \
  "SELECT * FROM sales_transactions WHERE created_at > NOW() - INTERVAL '24 hours';"
```

**Solution**:

Usually resolves after a few minutes (eventual consistency). If persistent:

```bash
# Restart backend
docker compose restart backend

# Check logs for exceptions
docker compose logs backend --tail 100
```

---

## Troubleshooting Workflows

### Workflow 1: User Cannot Start Shop Manager

```
User reports: "App won't start"

Step 1: Gather Information
├─ Platform? (Windows/macOS/Linux)
├─ Installation method? (Electron/Docker Compose)
├─ First time or was working before?
└─ Any error messages?

Step 2: Check Prerequisites
├─ Docker installed? → docker --version
├─ Docker running? → docker ps
└─ Sufficient resources? → docker stats

Step 3: Check Logs
├─ Electron: Check app logs
└─ Docker: docker compose logs

Step 4: Common Fixes
├─ Port conflict? → Change ports in config.yaml
├─ Permission issue? → Run with sudo (Linux) or as Administrator (Windows)
├─ Corrupted state? → docker compose down -v && docker compose up -d
└─ Out of date? → Update to latest version

Step 5: Escalate if Unresolved
└─ Collect system info, logs, screenshots → Create GitHub issue
```

---

### Workflow 2: User Reports Bug

```
User reports: "Sales report shows wrong total"

Step 1: Reproduce
├─ Can you reproduce on your system?
├─ Same version as user?
└─ Same data scenario?

Step 2: Gather Details
├─ Exact steps to reproduce
├─ Expected vs. actual result
├─ Screenshots if applicable
└─ Database state (if needed)

Step 3: Classify Severity
├─ Critical: Data corruption, security issue
├─ High: Core feature broken, many users affected
├─ Medium: Minor feature, workaround exists
└─ Low: Cosmetic, rare edge case

Step 4: Document
└─ Create GitHub issue with all details

Step 5: Workaround (if possible)
└─ Provide temporary solution while fix is being developed

Step 6: Fix & Release
├─ Develop fix
├─ Test thoroughly
└─ Release patch version
```

---

### Workflow 3: Feature Request

```
User requests: "Can you add barcode scanner support?"

Step 1: Clarify Request
├─ What problem does this solve?
├─ How would you use it?
└─ How critical is it?

Step 2: Check Existing
├─ Already implemented?
├─ Planned on roadmap?
└─ Similar request exists?

Step 3: Evaluate Feasibility
├─ Technical complexity
├─ Time estimate
├─ Alignment with product vision
└─ Benefit to other users

Step 4: Respond
├─ Already exists → Point to documentation
├─ On roadmap → Provide timeline
├─ Will consider → Add to backlog, gather votes
└─ Won't implement → Explain why, suggest alternatives

Step 5: Track
└─ Add to GitHub discussions or project board
```

---

## Knowledge Base

Create comprehensive documentation for common topics.

### KB Article Template

```markdown
# Title: How to Change Default Currency

**Category**: Configuration

**Tags**: currency, settings, international

**Last Updated**: 2024-01-15

---

## Problem

You want to change the default currency from USD to EUR (or another currency).

## Solution

### Method 1: During Initial Setup

1. Open the Setup Wizard when first launching Shop Manager
2. Step 2: Configuration
3. Select your preferred currency from the dropdown
4. Complete the wizard

### Method 2: After Installation

1. Stop Shop Manager:
   ```bash
   docker compose down
   ```

2. Edit `config.yaml`:
   ```yaml
   business:
     currency: EUR  # Change from USD
   ```

3. Regenerate configuration:
   ```bash
   python3 scripts/generate-config.py
   ```

4. Restart services:
   ```bash
   docker compose up -d
   ```

5. Verify: Open Shop Manager and check that prices display in EUR (€)

## Supported Currencies

- USD (US Dollar)
- EUR (Euro)
- GBP (British Pound)
- NGN (Nigerian Naira)
- More coming soon!

## Related Articles

- [How to Add Multiple Currencies](kb/multi-currency)
- [Currency Conversion Rates](kb/currency-rates)
- [Tax Configuration by Country](kb/tax-config)

## Still Need Help?

If this didn't solve your issue:
- Email: support@shopmanager.com
- Discord: #support channel
- GitHub: [Create an issue](https://github.com/yourorg/shop-manager/issues/new)
```

### Priority KB Articles (Create These First)

1. **Installation**
   - How to install on Windows
   - How to install on macOS
   - How to install on Linux
   - Docker Desktop installation guide
   - Troubleshooting installation errors

2. **Configuration**
   - How to change company name
   - How to upload custom logo
   - How to configure SSL certificates
   - How to add test users
   - How to change default ports

3. **Usage**
   - How to add a product
   - How to process a sale
   - How to manage inventory
   - How to generate reports
   - How to add users and assign roles

4. **Troubleshooting**
   - Cannot login
   - Services won't start
   - Port conflicts
   - Performance issues
   - Data backup and restore

5. **Advanced**
   - How to customize Keycloak realm
   - How to integrate with external systems
   - How to set up automatic backups
   - How to migrate data from other systems

---

## Support Escalation

### Escalation Levels

**Level 1: Community Support**
- Discord community
- Self-service documentation
- Automated chatbot (if implemented)

**Level 2: Support Team**
- Email support
- GitHub issues
- Standard response times

**Level 3: Engineering Team**
- Complex bugs requiring code changes
- Architecture questions
- Performance optimization

**Level 4: Product/Leadership**
- Feature prioritization
- Roadmap questions
- Strategic decisions

### When to Escalate

**To Level 2** (Support → Engineering):
- Bug requires code fix
- Issue not documented
- Complex technical question
- User blocked for >48 hours

**To Level 3** (Engineering → Product):
- Feature request with high demand
- Strategic technical decision
- Resource allocation needed

**To Level 4** (Product → Leadership):
- Major product direction
- Significant resource investment
- Legal/compliance questions

---

## User Communication Templates

### 1. First Response (Acknowledgment)

```
Subject: Re: [Issue Title]

Hi [Name],

Thank you for contacting Shop Manager support!

I've received your message about [brief summary]. I'm looking into this and will get back to you within [timeframe] with more information.

In the meantime, could you please provide:
- [Specific info needed 1]
- [Specific info needed 2]

This will help me investigate your issue more quickly.

Best regards,
[Your Name]
Shop Manager Support Team
```

---

### 2. Solution Provided

```
Subject: Re: [Issue Title]

Hi [Name],

Good news! I've found the solution to your issue.

[Explanation of the problem]

Here's how to fix it:

1. [Step 1]
2. [Step 2]
3. [Step 3]

[Screenshot or code snippet if helpful]

This should resolve the issue. Please let me know if you have any questions or if the problem persists.

Best regards,
[Your Name]
Shop Manager Support Team
```

---

### 3. Issue Escalated

```
Subject: Re: [Issue Title]

Hi [Name],

Thank you for your patience. I've escalated your issue to our engineering team for further investigation.

Here's what happens next:
- Our engineers will review the issue within 48 hours
- I'll keep you updated on progress
- Target resolution: [date or timeframe]

Issue Tracking: https://github.com/yourorg/shop-manager/issues/[number]

In the meantime, [workaround if available].

I'll follow up by [specific date].

Best regards,
[Your Name]
Shop Manager Support Team
```

---

### 4. Bug Confirmed

```
Subject: Re: [Issue Title]

Hi [Name],

Thank you for reporting this issue! I can confirm this is a bug in Shop Manager.

Details:
- **Affects**: Version 1.0.1 and earlier
- **Impact**: [describe impact]
- **Workaround**: [temporary solution if available]
- **Fix Timeline**: Will be included in v1.0.2 (estimated release: [date])

I've created a GitHub issue to track this: [link]

You'll be notified when the fix is released. Thank you for helping us improve Shop Manager!

Best regards,
[Your Name]
Shop Manager Support Team
```

---

### 5. Feature Request Response

```
Subject: Re: Feature Request - [Feature Name]

Hi [Name],

Thank you for your feature request! This is a great idea.

I've added it to our product backlog: [link to GitHub discussion/issue]

Next steps:
- Our product team will review and prioritize
- Community members can upvote and add comments
- We'll update you if this is scheduled for development

To increase visibility:
- Share the link with other users who might benefit
- Add any additional context or use cases

Thank you for helping shape the future of Shop Manager!

Best regards,
[Your Name]
Shop Manager Support Team
```

---

### 6. Cannot Reproduce

```
Subject: Re: [Issue Title]

Hi [Name],

Thank you for your report. I've attempted to reproduce this issue on my end, but I'm unable to see the same behavior.

To help me investigate further, could you please provide:

1. **Exact version**: Check Help → About (should be like v1.0.1)
2. **Platform details**:
   - Operating System: (e.g., Windows 11, macOS 14.2, Ubuntu 22.04)
   - Installation method: (Electron app, Docker Compose, etc.)
3. **Step-by-step reproduction**:
   - Starting from a specific page
   - Exact clicks/inputs
   - What you see vs. what you expect
4. **Screenshots or screen recording** if possible
5. **Logs**: Run `docker compose logs` and send output

This will help me understand what's different in your environment.

Best regards,
[Your Name]
Shop Manager Support Team
```

---

## Support Metrics

### Key Performance Indicators (KPIs)

Track these metrics weekly:

**Response Time**:
- First response time (median)
- Resolution time (median)
- % within SLA

**Volume**:
- Total tickets
- New tickets
- Closed tickets
- Backlog size

**Quality**:
- User satisfaction (CSAT score)
- Tickets reopened (%)
- Escalation rate (%)

**Topic Distribution**:
- Installation issues
- Configuration questions
- Bugs
- Feature requests
- Documentation gaps

### Weekly Support Report Template

```markdown
# Support Report - Week of [Date]

## Summary

- **Total Tickets**: 45 (+5% vs last week)
- **Closed Tickets**: 38
- **Backlog**: 12
- **Median Response Time**: 6 hours (target: 24h) ✅
- **CSAT Score**: 92% (target: 90%) ✅

## Top Issues This Week

1. **Port 3001 already in use** (8 tickets)
   - Solution: Added to FAQ
   - Action: Improve installer to detect port conflicts

2. **SSL Certificate errors on macOS** (6 tickets)
   - Solution: install-certs.sh script
   - Action: Update installation guide

3. **Cannot login after install** (5 tickets)
   - Root cause: Test users not created
   - Action: Bug fix in v1.0.2

## Feature Requests

- Barcode scanner support (4 requests)
- Mobile app (3 requests)
- Multi-language support (2 requests)

## Escalations

- Issue #123: Database migration failure → Engineering
- Issue #124: Performance degradation → Engineering

## Action Items

- [ ] Update FAQ with top 3 issues
- [ ] Create video tutorial for SSL setup
- [ ] Release patch for test user bug
- [ ] Plan barcode scanner feature (high demand)

## Community Highlights

- User @johndoe shared amazing custom dashboard on Discord!
- 15 new members joined Discord this week
- 5 community-answered questions (thank you!)
```

---

## Community Management

### Discord Best Practices

**Onboarding New Members**:
```
Welcome bot message:

👋 Welcome to Shop Manager, @username!

We're glad you're here! Here's how to get started:

📖 Read the docs: https://docs.shopmanager.com
❓ Need help? Ask in #support
💡 Ideas? Share in #feature-requests
🎉 Show off your setup in #showcase

Rules: Be respectful, helpful, and on-topic.

Enjoy! 🏪
```

**Encouraging Community Help**:
- Recognize helpful members with roles (Helper, Contributor)
- Highlight great answers in #community-highlights
- Monthly "Community MVP" award

**Handling Negative Feedback**:
1. Acknowledge the frustration
2. Ask for specific details
3. Explain what you'll do
4. Follow up with resolution
5. Never be defensive

---

### Managing GitHub Issues

**Triage Process** (run daily):

```bash
# Get new issues
gh issue list --label "triage" --limit 20

# For each issue:
# 1. Can you reproduce?
# 2. Is it a duplicate? → Close with reference
# 3. Is it a feature request? → Move to discussions
# 4. Is it valid bug? → Label and assign priority
# 5. Is info missing? → Ask for details
```

**Labels to Use**:
- `bug` - Confirmed bug
- `feature` - Feature request
- `documentation` - Docs improvement
- `good first issue` - Easy for newcomers
- `help wanted` - Community can contribute
- `critical` / `high` / `medium` / `low` - Priority
- `windows` / `macos` / `linux` - Platform-specific

---

## Next Steps

1. [Set Up Analytics](ANALYTICS_TRACKING.md) - Track support metrics
2. [Review Release Process](RELEASE_PROCESS.md) - Coordinate with releases
3. [Marketing Guide](MARKETING_GUIDE.md) - Proactive communication

---

## Resources

**Support Tools**:
- **Zendesk**: Ticketing system
- **Intercom**: Chat support
- **Freshdesk**: Help desk software
- **Help Scout**: Email-based support

**Knowledge Base Tools**:
- **GitBook**: Beautiful docs
- **Notion**: Flexible KB
- **Confluence**: Enterprise KB
- **ReadMe**: API docs + KB

**Community Tools**:
- **Discord**: Real-time chat
- **Slack**: Professional communities
- **Discourse**: Forum software
- **GitHub Discussions**: Developer communities

---

## Contact

For support team questions:
- **Email**: support@shopmanager.com
- **Slack**: #support-team channel
- **Lead**: support-lead@shopmanager.com
