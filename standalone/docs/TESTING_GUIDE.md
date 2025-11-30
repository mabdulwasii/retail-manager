# Shop Manager - Testing Guide

Comprehensive testing procedures for Shop Manager standalone packages before release.

## Table of Contents

1. [Testing Philosophy](#testing-philosophy)
2. [Pre-Release Testing Checklist](#pre-release-testing-checklist)
3. [Platform-Specific Testing](#platform-specific-testing)
4. [Feature Testing Matrix](#feature-testing-matrix)
5. [Performance Testing](#performance-testing)
6. [Security Testing](#security-testing)
7. [Upgrade Testing](#upgrade-testing)
8. [User Acceptance Testing](#user-acceptance-testing)
9. [Automated Testing](#automated-testing)
10. [Bug Reporting](#bug-reporting)

---

## Testing Philosophy

**Goals**:
- Zero critical bugs in production
- Smooth installation experience across all platforms
- Consistent behavior across Windows/macOS/Linux
- Performance meets user expectations
- Security vulnerabilities identified before release

**Testing Levels**:
1. **Unit Tests**: Already covered in backend/frontend codebases
2. **Integration Tests**: Service-to-service communication
3. **Package Tests**: Installation and configuration
4. **End-to-End Tests**: Full user workflows
5. **Performance Tests**: Load and stress testing
6. **Security Tests**: Vulnerability scanning

---

## Pre-Release Testing Checklist

Before releasing any version:

### Build Verification
- [ ] All platform packages built successfully
- [ ] No build warnings or errors
- [ ] All checksums generated correctly
- [ ] File sizes within expected ranges:
  - Lightweight package: < 100 MB
  - Full package: 1.5-2.5 GB
  - Windows installer: < 150 MB
  - macOS DMG: < 150 MB
  - Linux AppImage: < 150 MB

### Documentation
- [ ] README.md updated with correct version
- [ ] CHANGELOG.md includes all changes
- [ ] Installation instructions tested
- [ ] Customization guide accurate
- [ ] Known issues documented

### Configuration
- [ ] config.yaml has sensible defaults
- [ ] Test users work correctly
- [ ] SSL certificates generate properly
- [ ] Environment variables set correctly

### Services
- [ ] All Docker containers start successfully
- [ ] Health checks pass for all services
- [ ] Keycloak realm imports correctly
- [ ] Database migrations run successfully
- [ ] Kafka topics created

### Core Functionality
- [ ] User login works
- [ ] Dashboard loads
- [ ] POS system operational
- [ ] Inventory management works
- [ ] Reports generate correctly
- [ ] PDF receipts print properly

---

## Platform-Specific Testing

### Windows Testing

**Test Environments**:
- Windows 11 (latest)
- Windows 10 22H2
- Windows Server 2022 (for enterprise)

**Installation Tests**:

```powershell
# Test 1: Clean Windows 11 Installation
# Prerequisites:
# - Fresh Windows 11 VM
# - No Docker Desktop installed

# Download installer
Invoke-WebRequest -Uri "https://shopmanager.com/download/windows" -OutFile "Shop-Manager-Setup-1.0.0.exe"

# Verify checksum
Get-FileHash "Shop-Manager-Setup-1.0.0.exe" -Algorithm SHA256

# Run installer
Start-Process "Shop-Manager-Setup-1.0.0.exe" -Wait

# Verify installation
Test-Path "C:\Program Files\Shop Manager\Shop Manager.exe"

# Launch application
Start-Process "C:\Program Files\Shop Manager\Shop Manager.exe"
```

**Expected Results**:
- [ ] Installer runs without errors
- [ ] Docker Desktop installation prompts if not present
- [ ] Application shortcut created on Desktop
- [ ] Start Menu entry created
- [ ] Application launches successfully
- [ ] System tray icon appears
- [ ] Setup wizard guides through configuration
- [ ] Services start within 2 minutes

**Common Issues**:
- WSL 2 not enabled → Installer should prompt and guide
- Hyper-V disabled → Installer should enable and prompt for restart
- Insufficient disk space → Clear error message before installation
- Antivirus blocking → Instructions in troubleshooting docs

---

### macOS Testing

**Test Environments**:
- macOS Sonoma (14.x) - Intel
- macOS Sonoma (14.x) - Apple Silicon
- macOS Ventura (13.x)

**Installation Tests**:

```bash
# Test 1: Clean macOS Sonoma (Apple Silicon)
# Prerequisites:
# - Fresh macOS VM or test Mac
# - No Docker Desktop installed

# Download DMG
curl -L "https://shopmanager.com/download/macos" -o "Shop-Manager-1.0.0.dmg"

# Verify checksum
shasum -a 256 "Shop-Manager-1.0.0.dmg"

# Mount DMG
hdiutil attach "Shop-Manager-1.0.0.dmg"

# Copy to Applications
cp -R "/Volumes/Shop Manager/Shop Manager.app" /Applications/

# Unmount
hdiutil detach "/Volumes/Shop Manager"

# Launch application
open "/Applications/Shop Manager.app"
```

**Expected Results**:
- [ ] DMG mounts without issues
- [ ] Application copies to /Applications
- [ ] No Gatekeeper warnings (if notarized)
- [ ] Application launches successfully
- [ ] Docker Desktop installation prompts if not present
- [ ] Menu bar icon appears
- [ ] Setup wizard works correctly
- [ ] Services start within 2 minutes

**macOS-Specific Checks**:
- [ ] Code signature valid: `codesign -dv "/Applications/Shop Manager.app"`
- [ ] Notarization successful: `spctl -a -vv "/Applications/Shop Manager.app"`
- [ ] Rosetta 2 not required on Apple Silicon
- [ ] Menu bar icon renders correctly in both light/dark mode
- [ ] Keychain access works for certificate storage

---

### Linux Testing

**Test Distributions**:
- Ubuntu 22.04 LTS (most common)
- Ubuntu 24.04 LTS
- Debian 12
- Fedora 39
- Arch Linux (optional - for enthusiasts)

**Installation Tests**:

```bash
# Test 1: Ubuntu 22.04 Fresh Install
# Prerequisites:
# - Fresh Ubuntu 22.04 VM
# - No Docker installed

# Download AppImage
wget "https://shopmanager.com/download/linux" -O "Shop-Manager-1.0.0.AppImage"

# Verify checksum
sha256sum "Shop-Manager-1.0.0.AppImage"

# Make executable
chmod +x "Shop-Manager-1.0.0.AppImage"

# Run AppImage
./Shop-Manager-1.0.0.AppImage
```

**Test 2: Debian Package**:

```bash
# Download .deb
wget "https://shopmanager.com/download/linux/deb" -O "shop-manager_1.0.0_amd64.deb"

# Install
sudo dpkg -i shop-manager_1.0.0_amd64.deb
sudo apt-get install -f  # Fix dependencies

# Launch
shop-manager
```

**Test 3: RPM Package (Fedora)**:

```bash
# Download .rpm
wget "https://shopmanager.com/download/linux/rpm" -O "shop-manager-1.0.0.x86_64.rpm"

# Install
sudo dnf install shop-manager-1.0.0.x86_64.rpm

# Launch
shop-manager
```

**Expected Results**:
- [ ] AppImage runs on all distributions
- [ ] No missing library errors
- [ ] FUSE works or fallback method used
- [ ] .deb installs on Debian/Ubuntu
- [ ] .rpm installs on Fedora/RHEL
- [ ] Desktop entry created (`~/.local/share/applications/`)
- [ ] Application appears in launcher
- [ ] Docker installation prompts if not present
- [ ] Services start correctly

**Linux-Specific Checks**:
- [ ] Permissions set correctly
- [ ] SELinux doesn't block (if enabled)
- [ ] AppArmor doesn't block (if enabled)
- [ ] Systemd service created (for auto-start)
- [ ] XDG directories used correctly

---

## Feature Testing Matrix

Test each feature systematically across all platforms.

### 1. User Authentication

| Test Case | Steps | Expected Result | Windows | macOS | Linux |
|-----------|-------|-----------------|---------|-------|-------|
| Login with test user | 1. Open app<br>2. Click "Open Dashboard"<br>3. Login with admin@shopmanager.com | Dashboard loads | [ ] | [ ] | [ ] |
| Invalid credentials | Login with wrong password | Error message displayed | [ ] | [ ] | [ ] |
| Session timeout | Wait 30 minutes idle | Auto-logout, redirect to login | [ ] | [ ] | [ ] |
| Logout | Click logout | Redirect to login page | [ ] | [ ] | [ ] |

### 2. Point of Sale

| Test Case | Steps | Expected Result | Windows | macOS | Linux |
|-----------|-------|-----------------|---------|-------|-------|
| Add product to cart | POS → Search product → Add | Product in cart | [ ] | [ ] | [ ] |
| Remove from cart | Click remove icon | Product removed | [ ] | [ ] | [ ] |
| Apply discount | Enter discount code | Discount applied | [ ] | [ ] | [ ] |
| Complete sale | Click "Complete Sale" | Receipt generated | [ ] | [ ] | [ ] |
| Print receipt | Click "Print" | PDF opens | [ ] | [ ] | [ ] |
| Cash payment | Select cash → Enter amount | Change calculated | [ ] | [ ] | [ ] |
| Card payment | Select card | Payment processed | [ ] | [ ] | [ ] |

### 3. Inventory Management

| Test Case | Steps | Expected Result | Windows | macOS | Linux |
|-----------|-------|-----------------|---------|-------|-------|
| View inventory | Navigate to Inventory | List displayed | [ ] | [ ] | [ ] |
| Add product | Click Add → Fill form → Save | Product created | [ ] | [ ] | [ ] |
| Edit product | Click Edit → Modify → Save | Changes saved | [ ] | [ ] | [ ] |
| Delete product | Click Delete → Confirm | Product deleted | [ ] | [ ] | [ ] |
| Low stock alert | Reduce stock below threshold | Alert appears | [ ] | [ ] | [ ] |
| Batch tracking | Add batch number | Batch tracked | [ ] | [ ] | [ ] |
| Expiry date | Set expiry date | FEFO logic works | [ ] | [ ] | [ ] |

### 4. Sales Reports

| Test Case | Steps | Expected Result | Windows | macOS | Linux |
|-----------|-------|-----------------|---------|-------|-------|
| Daily sales report | Reports → Daily | Report shows | [ ] | [ ] | [ ] |
| Date range filter | Select date range | Filtered results | [ ] | [ ] | [ ] |
| Export to PDF | Click Export → PDF | PDF downloads | [ ] | [ ] | [ ] |
| Export to Excel | Click Export → Excel | Excel downloads | [ ] | [ ] | [ ] |
| Sales by product | View product breakdown | Accurate totals | [ ] | [ ] | [ ] |
| Sales by category | View category breakdown | Accurate totals | [ ] | [ ] | [ ] |

### 5. User Management

| Test Case | Steps | Expected Result | Windows | macOS | Linux |
|-----------|-------|-----------------|---------|-------|-------|
| Create user | Users → Add → Save | User created | [ ] | [ ] | [ ] |
| Assign role | Edit user → Select role | Role assigned | [ ] | [ ] | [ ] |
| Deactivate user | Click Deactivate | User disabled | [ ] | [ ] | [ ] |
| Reset password | Click Reset Password | Email sent | [ ] | [ ] | [ ] |
| Permission check | Login as cashier → Try admin action | Access denied | [ ] | [ ] | [ ] |

---

## Performance Testing

### Load Testing

**Objective**: Ensure system handles expected load

**Test Scenarios**:

**Scenario 1: Concurrent Sales**
```bash
# Simulate 10 concurrent cashiers
# Each processing 1 sale per minute
# For 1 hour

# Expected: No errors, <2s response time
```

**Scenario 2: Large Inventory**
```bash
# Import 10,000 products
# Navigate inventory list
# Search products

# Expected: <3s load time, smooth scrolling
```

**Scenario 3: Report Generation**
```bash
# Generate sales report for 1 year
# 50,000+ transactions

# Expected: <10s generation time
```

**Performance Benchmarks**:

| Metric | Target | Acceptable | Poor |
|--------|--------|------------|------|
| Dashboard load | <1s | <2s | >3s |
| POS sale completion | <2s | <4s | >5s |
| Inventory search | <500ms | <1s | >2s |
| Report generation (30 days) | <3s | <5s | >10s |
| Login | <1s | <2s | >3s |

**Testing Tools**:

```bash
# Apache Bench (simple load test)
ab -n 1000 -c 10 http://localhost:8081/api/products

# k6 (advanced scenarios)
k6 run load-test.js

# Expected output:
# - 0% error rate
# - p95 response time < 2s
# - p99 response time < 5s
```

### Resource Usage

**Idle State** (no active users):
- RAM: < 2 GB
- CPU: < 5%
- Disk I/O: Minimal

**Active State** (5 users, light usage):
- RAM: < 4 GB
- CPU: < 20%
- Disk I/O: Moderate

**Peak Load** (10 users, heavy usage):
- RAM: < 8 GB
- CPU: < 50%
- Disk I/O: High but not saturated

**Monitoring Commands**:

```bash
# Docker stats
docker stats

# Expected output:
# backend: <2GB RAM, <30% CPU
# frontend: <500MB RAM, <10% CPU
# postgres: <1GB RAM, <20% CPU
# keycloak: <1.5GB RAM, <15% CPU
```

---

## Security Testing

### Vulnerability Scanning

**1. Container Scanning**

```bash
# Scan Docker images for vulnerabilities
docker scout cve shop-manager-backend:latest
docker scout cve shop-manager-frontend:latest

# Expected: No HIGH or CRITICAL vulnerabilities
```

**2. Dependency Scanning**

```bash
# Backend (Maven)
./mvnw dependency-check:check

# Frontend (npm)
npm audit

# Expected: No vulnerabilities or all documented as false positives
```

**3. SSL/TLS Testing**

```bash
# Test SSL configuration
nmap --script ssl-enum-ciphers -p 443 localhost

# Expected:
# - TLS 1.2 or 1.3 only
# - Strong cipher suites
# - No weak algorithms
```

### Penetration Testing Checklist

- [ ] SQL Injection: Test all input fields
- [ ] XSS: Test HTML/JS injection in forms
- [ ] CSRF: Verify CSRF tokens on all forms
- [ ] Authentication bypass: Test role enforcement
- [ ] Session hijacking: Test token security
- [ ] File upload: Test malicious file uploads
- [ ] API security: Test unauthorized API access
- [ ] Rate limiting: Test brute force protection

**Tools**:
- **OWASP ZAP**: Web vulnerability scanner
- **Burp Suite**: Manual penetration testing
- **Nikto**: Web server scanner

```bash
# OWASP ZAP automated scan
zap-cli quick-scan http://localhost:3001

# Expected: No HIGH risk findings
```

### Security Best Practices Verification

- [ ] All passwords hashed (bcrypt)
- [ ] JWT tokens signed and validated
- [ ] HTTPS enforced
- [ ] CORS configured correctly
- [ ] SQL queries parameterized
- [ ] File permissions restrictive
- [ ] No secrets in code/config
- [ ] Audit logs for sensitive operations

---

## Upgrade Testing

### Version Upgrade Path

Test upgrading from previous versions:

**Scenario 1: v1.0.0 → v1.1.0** (minor update)

```bash
# 1. Install v1.0.0
./install.sh

# 2. Create test data
# - Add 50 products
# - Create 10 sales
# - Add 3 users

# 3. Backup data
docker compose exec postgres pg_dump -U shopmanager > backup-v1.0.0.sql

# 4. Upgrade to v1.1.0
./upgrade.sh v1.1.0

# 5. Verify
# - All data intact
# - New features work
# - No errors in logs
```

**Expected Results**:
- [ ] Zero data loss
- [ ] All transactions preserved
- [ ] Users can login
- [ ] New features available
- [ ] < 5 minutes downtime

**Scenario 2: v1.0.0 → v2.0.0** (major update)

```bash
# Same process, but allow for:
# - Breaking changes documented
# - Migration scripts run successfully
# - < 15 minutes downtime
```

### Rollback Testing

```bash
# Test rolling back from v1.1.0 to v1.0.0
./rollback.sh v1.0.0

# Verify:
# - Application works
# - Data intact (may lose v1.1.0-specific data)
# - Clear warnings about potential data loss
```

---

## User Acceptance Testing

### Beta Testing Program

**Recruit 10-20 beta testers**:
- 5 small retail shops (actual target users)
- 5 technical users (developers, sysadmins)
- 5 non-technical users (shop owners with no tech background)
- 5 diverse platforms (mix of Windows/macOS/Linux)

**Beta Test Duration**: 2 weeks

**Feedback Collection**:

```markdown
# Beta Test Feedback Form

**Your Background**:
- Role: [ ] Shop Owner [ ] Developer [ ] Other
- Technical Level: [ ] Beginner [ ] Intermediate [ ] Advanced
- Platform: [ ] Windows [ ] macOS [ ] Linux

**Installation (1-5 stars)**:
- Ease of installation: ⭐⭐⭐⭐⭐
- Time to install: _____ minutes
- Issues encountered: ___________

**Features (1-5 stars)**:
- Dashboard usability: ⭐⭐⭐⭐⭐
- POS system: ⭐⭐⭐⭐⭐
- Inventory management: ⭐⭐⭐⭐⭐
- Reports: ⭐⭐⭐⭐⭐

**Performance**:
- Overall speed: [ ] Fast [ ] Acceptable [ ] Slow
- Crashes/errors: [ ] None [ ] Rare [ ] Frequent

**Would you recommend to a friend?**
[ ] Yes [ ] Maybe [ ] No

**Most valuable feature**:
_____________________

**Biggest pain point**:
_____________________

**Feature requests**:
_____________________
```

### Acceptance Criteria

Before release, ensure:
- [ ] 80%+ testers rate installation 4+ stars
- [ ] 80%+ testers rate core features 4+ stars
- [ ] 90%+ testers would recommend to a friend
- [ ] No critical bugs reported
- [ ] All high-priority bugs fixed
- [ ] Documentation clear to 80%+ testers

---

## Automated Testing

### GitHub Actions Test Matrix

`.github/workflows/test-packages.yml`:

```yaml
name: Test Packages

on:
  push:
    branches: [main]
  pull_request:

jobs:
  test-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install
        run: |
          ./standalone/install.bat
      - name: Health Check
        run: |
          docker compose ps
      - name: Test Login
        run: |
          curl http://localhost:3001/health

  test-macos:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install
        run: |
          ./standalone/install.sh
      - name: Health Check
        run: |
          docker compose ps
      - name: Test Login
        run: |
          curl http://localhost:3001/health

  test-linux:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install
        run: |
          ./standalone/install.sh
      - name: Health Check
        run: |
          docker compose ps
      - name: Test Login
        run: |
          curl http://localhost:3001/health
```

### Smoke Tests

Run after every deployment:

```bash
#!/bin/bash
# smoke-test.sh

echo "🧪 Running smoke tests..."

# Test 1: Services running
if ! docker compose ps | grep -q "Up"; then
  echo "❌ Services not running"
  exit 1
fi

# Test 2: Backend health
if ! curl -f http://localhost:8081/actuator/health; then
  echo "❌ Backend health check failed"
  exit 1
fi

# Test 3: Frontend accessible
if ! curl -f http://localhost:3001; then
  echo "❌ Frontend not accessible"
  exit 1
fi

# Test 4: Keycloak up
if ! curl -f http://localhost:8080/realms/shop-manager; then
  echo "❌ Keycloak not accessible"
  exit 1
fi

# Test 5: Database connection
if ! docker compose exec -T postgres pg_isready; then
  echo "❌ Database not ready"
  exit 1
fi

echo "✅ All smoke tests passed!"
```

---

## Bug Reporting

### Bug Severity Levels

**Critical** (fix immediately):
- Data loss
- Security vulnerability
- Complete system failure
- Installation impossible

**High** (fix before release):
- Core feature broken
- Frequent crashes
- Major performance issues
- Incorrect calculations

**Medium** (fix soon):
- Minor feature broken
- UI glitches
- Moderate performance issues
- Workaround available

**Low** (fix when time permits):
- Cosmetic issues
- Enhancement requests
- Documentation typos

### Bug Report Template

```markdown
# Bug Report

**Severity**: [ ] Critical [ ] High [ ] Medium [ ] Low

**Platform**:
- OS: Windows 11 / macOS 14 / Ubuntu 22.04
- Version: 1.0.0
- Installation Method: Electron / Docker Compose

**Description**:
A clear description of what happened.

**Steps to Reproduce**:
1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

**Expected Behavior**:
What you expected to happen.

**Actual Behavior**:
What actually happened.

**Screenshots**:
If applicable, attach screenshots.

**Logs**:
```
Paste relevant logs here
```

**Additional Context**:
Any other information that might help.
```

### Bug Tracking

Use GitHub Issues with labels:
- `bug` - Confirmed bug
- `critical` - Critical severity
- `high` - High priority
- `medium` - Medium priority
- `low` - Low priority
- `windows` / `macos` / `linux` - Platform-specific
- `duplicate` - Duplicate issue
- `wontfix` - Won't be fixed

---

## Testing Environments

### Recommended VM Setup

For comprehensive testing:

**Windows**:
```
VMware/VirtualBox VM
- Windows 11 Pro
- 8 GB RAM
- 2 CPU cores
- 50 GB disk
- Clean install (no dev tools)
```

**macOS**:
```
macOS Sonoma VM (if on Mac) or actual hardware
- 8 GB RAM
- 2 CPU cores
- 50 GB disk
```

**Linux**:
```
Ubuntu 22.04 LTS VM
- 4 GB RAM
- 2 CPU cores
- 30 GB disk
- Minimal installation
```

### Cloud Testing Services

**BrowserStack** (cross-browser testing):
- Test across different OS versions
- Real devices, not emulators
- $29-99/month

**AWS EC2** (cheap testing VMs):
- Spin up Windows/Linux VMs on-demand
- ~$0.10/hour for t3.medium
- Snapshot clean state for repeated testing

---

## Release Sign-Off Checklist

Before marking a release as production-ready:

### Code Quality
- [ ] All tests passing (unit, integration, e2e)
- [ ] Code coverage > 80%
- [ ] No critical code smells (SonarQube)
- [ ] No security vulnerabilities (Snyk, OWASP)

### Functionality
- [ ] All features in release notes working
- [ ] No critical or high bugs
- [ ] Regression tests pass
- [ ] Upgrade path tested

### Platforms
- [ ] Windows installer tested
- [ ] macOS DMG tested
- [ ] Linux AppImage tested
- [ ] All packages signed/notarized

### Performance
- [ ] Load testing passed
- [ ] Resource usage within limits
- [ ] No memory leaks
- [ ] Startup time < 2 minutes

### Documentation
- [ ] README accurate
- [ ] CHANGELOG complete
- [ ] Installation guide updated
- [ ] Troubleshooting guide current

### Beta Testing
- [ ] 10+ beta testers
- [ ] 80%+ satisfaction
- [ ] All beta feedback addressed
- [ ] Testimonials collected

### Marketing
- [ ] Screenshots updated
- [ ] Demo video current
- [ ] Landing page updated
- [ ] Social media ready

---

## Next Steps

After completing testing:
1. [Create Release](RELEASE_PROCESS.md)
2. [Deploy to Distribution Channels](DEPLOYMENT_GUIDE.md)
3. [Launch Marketing Campaign](MARKETING_GUIDE.md)
4. [Set Up Customer Support](CUSTOMER_SUPPORT.md)

---

## Support

For testing questions or to report bugs:
- **GitHub Issues**: https://github.com/yourorg/shop-manager/issues
- **Email**: support@shopmanager.com
- **Discord**: #testing channel
