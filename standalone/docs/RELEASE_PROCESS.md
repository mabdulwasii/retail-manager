# Shop Manager - Release Process

Complete guide for managing Shop Manager releases from planning to post-release.

## Table of Contents

1. [Release Philosophy](#release-philosophy)
2. [Version Numbering](#version-numbering)
3. [Release Types](#release-types)
4. [Pre-Release Preparation](#pre-release-preparation)
5. [Building Release Packages](#building-release-packages)
6. [Release Checklist](#release-checklist)
7. [GitHub Release Process](#github-release-process)
8. [Post-Release Activities](#post-release-activities)
9. [Hotfix Process](#hotfix-process)
10. [Rollback Procedures](#rollback-procedures)

---

## Release Philosophy

**Principles**:
- **Stability First**: Never sacrifice stability for features
- **Backwards Compatible**: Maintain compatibility where possible
- **Tested Thoroughly**: All releases pass full test suite
- **Well Documented**: Clear changelog and upgrade guides
- **Timely Communication**: Users notified before and after releases

**Release Frequency**:
- **Major releases**: Every 6-12 months (breaking changes)
- **Minor releases**: Every 1-3 months (new features)
- **Patch releases**: As needed (bug fixes)
- **Security releases**: Immediately when required

---

## Version Numbering

We follow **Semantic Versioning 2.0.0** (https://semver.org)

### Format: `MAJOR.MINOR.PATCH`

**MAJOR** (1.0.0 → 2.0.0):
- Breaking changes to API or configuration
- Database schema changes requiring migration
- Removal of deprecated features
- Major architecture changes

Examples:
- v1.0.0 → v2.0.0: New authentication system (breaking)
- v2.0.0 → v3.0.0: Removed old API endpoints

**MINOR** (1.0.0 → 1.1.0):
- New features (backwards compatible)
- Deprecations (with warnings)
- Performance improvements
- Database additions (non-breaking)

Examples:
- v1.0.0 → v1.1.0: Added email notifications
- v1.1.0 → v1.2.0: New analytics dashboard

**PATCH** (1.0.0 → 1.0.1):
- Bug fixes
- Security patches
- Documentation updates
- Minor UI improvements

Examples:
- v1.0.0 → v1.0.1: Fixed login bug
- v1.0.1 → v1.0.2: Security patch

### Pre-Release Versions

**Alpha** (1.0.0-alpha.1):
- Internal testing only
- Unstable, features incomplete
- Frequent changes

**Beta** (1.0.0-beta.1):
- Public testing
- Feature complete
- Bug fixes and polish

**Release Candidate** (1.0.0-rc.1):
- Final testing before release
- No new features
- Only critical bug fixes

### Examples

```
1.0.0-alpha.1  → First alpha
1.0.0-alpha.2  → Second alpha
1.0.0-beta.1   → First beta
1.0.0-beta.2   → Second beta
1.0.0-rc.1     → Release candidate
1.0.0          → Stable release
1.0.1          → Patch release
1.1.0          → Minor release
2.0.0          → Major release
```

---

## Release Types

### 1. Major Release (e.g., v1.0.0 → v2.0.0)

**When to use**:
- Incompatible API changes
- Major feature rewrites
- Database schema overhaul
- Technology stack changes

**Timeline**: 6-12 months

**Process**:
1. Planning (1-2 months)
2. Development (3-6 months)
3. Alpha testing (2-4 weeks)
4. Beta testing (4-8 weeks)
5. RC testing (1-2 weeks)
6. Release

**Communication**:
- Announce plans 3 months ahead
- Blog post explaining changes
- Migration guide published
- Deprecation warnings in previous version

---

### 2. Minor Release (e.g., v1.0.0 → v1.1.0)

**When to use**:
- New features
- Performance improvements
- Non-breaking enhancements

**Timeline**: 1-3 months

**Process**:
1. Planning (1 week)
2. Development (3-8 weeks)
3. Beta testing (1-2 weeks)
4. Release

**Communication**:
- Announce in monthly newsletter
- Update roadmap
- Release notes

---

### 3. Patch Release (e.g., v1.0.0 → v1.0.1)

**When to use**:
- Bug fixes
- Security patches
- Minor improvements

**Timeline**: As needed (1-2 weeks)

**Process**:
1. Fix developed and tested
2. Create hotfix branch
3. Build and test packages
4. Release immediately

**Communication**:
- GitHub release notes
- Email to critical users (security)
- Tweet/social media

---

## Pre-Release Preparation

### 6 Weeks Before Release

**Planning**:
- [ ] Feature freeze date set
- [ ] Release date announced internally
- [ ] Release manager assigned
- [ ] Testing resources allocated

**Development**:
- [ ] All features completed
- [ ] Code review backlog cleared
- [ ] Technical debt addressed
- [ ] Dependencies updated

**Documentation**:
- [ ] User docs updated
- [ ] API docs current
- [ ] Migration guides drafted
- [ ] Release notes started

---

### 4 Weeks Before Release

**Code Freeze**:
- [ ] No new features (only bug fixes)
- [ ] Version bumped in code
- [ ] CHANGELOG.md updated
- [ ] README.md reviewed

**Testing**:
- [ ] Full test suite passing
- [ ] Performance tests run
- [ ] Security scan completed
- [ ] Manual testing started

**Build Preparation**:
- [ ] Build scripts tested
- [ ] Code signing certs valid
- [ ] Notarization working (macOS)
- [ ] CI/CD pipeline verified

---

### 2 Weeks Before Release

**Beta Release**:
- [ ] Beta packages built
- [ ] Shared with beta testers
- [ ] Feedback collected
- [ ] Critical issues fixed

**Documentation**:
- [ ] Changelog finalized
- [ ] Upgrade guide complete
- [ ] Breaking changes documented
- [ ] FAQ updated

**Marketing**:
- [ ] Screenshots updated
- [ ] Demo video recorded
- [ ] Blog post drafted
- [ ] Social media scheduled

---

### 1 Week Before Release

**Release Candidate**:
- [ ] RC packages built
- [ ] Final testing completed
- [ ] All critical bugs fixed
- [ ] Sign-off from QA team

**Preparation**:
- [ ] Release notes finalized
- [ ] Download links prepared
- [ ] CDN warmed up
- [ ] Support team briefed

**Communication**:
- [ ] Blog post scheduled
- [ ] Email draft ready
- [ ] Social media posts ready
- [ ] GitHub release drafted

---

## Building Release Packages

### 1. Version Bump

Update version in all files:

```bash
# Update package.json (Electron app)
cd standalone/electron-app
npm version 1.1.0 --no-git-tag-version

# Update config.yaml
cd ../
sed -i 's/version: .*/version: 1.1.0/' config.yaml

# Update Maven (backend - if relevant to standalone)
cd ../../backend
./mvnw versions:set -DnewVersion=1.1.0
```

### 2. Update Changelog

Edit `CHANGELOG.md`:

```markdown
# Changelog

All notable changes to Shop Manager will be documented in this file.

## [1.1.0] - 2024-02-15

### Added
- Email notification system for low stock alerts
- Export reports to Excel format
- Multi-currency support (USD, EUR, GBP)
- Dark mode for POS interface

### Changed
- Improved dashboard loading speed (40% faster)
- Updated Keycloak to 23.0.4
- Refined permission matrix for better granularity

### Fixed
- Fixed inventory count mismatch in batch tracking
- Resolved PDF receipt alignment issues on macOS
- Fixed session timeout not working correctly

### Security
- Updated Spring Boot to 3.2.2 (CVE-2024-12345)
- Patched PostgreSQL driver vulnerability

## [1.0.1] - 2024-01-20

### Fixed
- Critical bug in sales calculation
- Memory leak in analytics service

## [1.0.0] - 2024-01-10

### Added
- Initial release
- Complete POS system
- Inventory management with batch tracking
- Multi-tenant support
- Keycloak authentication
- PDF receipt generation
```

### 3. Create Git Tag

```bash
# Commit version changes
git add .
git commit -m "chore: bump version to 1.1.0"

# Create annotated tag
git tag -a v1.1.0 -m "Release version 1.1.0

Major changes:
- Email notifications
- Multi-currency support
- Performance improvements

See CHANGELOG.md for full details"

# Push commits and tags
git push origin main
git push origin v1.1.0
```

### 4. Build Packages

```bash
cd standalone/scripts

# Build all packages
./create-distribution.sh --version 1.1.0 --include-images

# Verify checksums
cd dist
sha256sum -c *.sha256

# Expected files:
# - shop-manager-standalone-v1.1.0.zip
# - shop-manager-standalone-v1.1.0-full.zip
# - Shop-Manager-Setup-1.1.0.exe
# - Shop-Manager-1.1.0.dmg
# - Shop-Manager-1.1.0.AppImage
# - shop-manager_1.1.0_amd64.deb
# - shop-manager-1.1.0.x86_64.rpm
```

### 5. Test Packages

```bash
# Test each package on fresh VM
# See TESTING_GUIDE.md for complete procedures

# Windows
./Shop-Manager-Setup-1.1.0.exe

# macOS
open Shop-Manager-1.1.0.dmg

# Linux
chmod +x Shop-Manager-1.1.0.AppImage
./Shop-Manager-1.1.0.AppImage
```

---

## Release Checklist

Before releasing, verify ALL items:

### Code & Build
- [ ] Version bumped in all files
- [ ] Git tag created and pushed
- [ ] All packages built successfully
- [ ] Checksums generated
- [ ] Code signing completed (Windows, macOS)
- [ ] Notarization completed (macOS)
- [ ] All platforms tested

### Testing
- [ ] Unit tests: 100% passing
- [ ] Integration tests: 100% passing
- [ ] E2E tests: 100% passing
- [ ] Manual testing completed
- [ ] Performance tests passed
- [ ] Security scan clean
- [ ] Beta feedback addressed

### Documentation
- [ ] CHANGELOG.md complete
- [ ] README.md updated
- [ ] Upgrade guide written
- [ ] Breaking changes documented
- [ ] API docs current
- [ ] Screenshots updated

### Marketing
- [ ] Release notes written
- [ ] Blog post ready
- [ ] Social media posts scheduled
- [ ] Email newsletter prepared
- [ ] Demo video updated

### Infrastructure
- [ ] CDN configured
- [ ] Download links working
- [ ] GitHub release drafted
- [ ] Website updated
- [ ] Analytics tracking ready

### Team
- [ ] Support team trained
- [ ] FAQ updated
- [ ] Known issues documented
- [ ] Rollback plan ready

---

## GitHub Release Process

### 1. Create Draft Release

Go to GitHub → Releases → Draft a new release

**Tag**: `v1.1.0`

**Title**: `Shop Manager v1.1.0 - Email Notifications & Multi-Currency`

**Description**:

```markdown
# Shop Manager v1.1.0

We're excited to announce Shop Manager 1.1.0 with email notifications, multi-currency support, and major performance improvements!

## ✨ What's New

### Email Notifications
Stay informed with automatic alerts for:
- Low stock warnings
- Daily sales summaries
- New user signups

### Multi-Currency Support
Now supports USD, EUR, and GBP with automatic conversion.

### Performance Improvements
- Dashboard loads 40% faster
- Improved report generation speed
- Reduced memory usage

## 🐛 Bug Fixes

- Fixed inventory count mismatch in batch tracking
- Resolved PDF receipt alignment issues on macOS
- Fixed session timeout not working correctly

## 🔒 Security Updates

- Updated Spring Boot to 3.2.2 (addresses CVE-2024-12345)
- Patched PostgreSQL driver vulnerability

## 📦 Downloads

Choose the installer for your platform:

| Platform | Package | Size |
|----------|---------|------|
| **Windows** | [Shop-Manager-Setup-1.1.0.exe](link) | 135 MB |
| **macOS** | [Shop-Manager-1.1.0.dmg](link) | 128 MB |
| **Linux** | [Shop-Manager-1.1.0.AppImage](link) | 142 MB |

### Alternative Downloads

- [Debian/Ubuntu (.deb)](link) - 140 MB
- [Fedora/RHEL (.rpm)](link) - 145 MB
- [Docker Compose (lightweight)](link) - 50 MB
- [Docker Compose (full offline)](link) - 2.1 GB

### Checksums

```
SHA256:
abc123...  Shop-Manager-Setup-1.1.0.exe
def456...  Shop-Manager-1.1.0.dmg
ghi789...  Shop-Manager-1.1.0.AppImage
```

## 📚 Documentation

- [Installation Guide](https://shopmanager.com/docs/install)
- [Upgrade Guide](https://shopmanager.com/docs/upgrade)
- [Changelog](https://github.com/yourorg/shop-manager/blob/main/CHANGELOG.md)

## 🔄 Upgrading from v1.0.x

```bash
# Backup your data first!
docker compose exec postgres pg_dump -U shopmanager > backup.sql

# Stop services
docker compose down

# Download new version
# Extract and run upgrade script
./upgrade.sh v1.1.0
```

**Note**: This is a minor release. Upgrading from v1.0.x is seamless with no breaking changes.

## 🆕 What's Coming Next

Preview of v1.2.0:
- Customer loyalty program
- Advanced analytics dashboard
- Mobile app (iOS/Android)
- Barcode scanner integration

## 💬 Community

- **Website**: https://shopmanager.com
- **Docs**: https://docs.shopmanager.com
- **Discord**: https://discord.gg/shopmanager
- **Email**: support@shopmanager.com

## 🙏 Thank You

Special thanks to our beta testers and contributors who made this release possible!

---

**Full Changelog**: https://github.com/yourorg/shop-manager/compare/v1.0.0...v1.1.0
```

### 2. Upload Artifacts

Drag and drop or upload:
- `Shop-Manager-Setup-1.1.0.exe`
- `Shop-Manager-1.1.0.dmg`
- `Shop-Manager-1.1.0.AppImage`
- `shop-manager_1.1.0_amd64.deb`
- `shop-manager-1.1.0.x86_64.rpm`
- `shop-manager-standalone-v1.1.0.zip`
- `shop-manager-standalone-v1.1.0-full.zip`
- All `.sha256` checksum files

### 3. Publish Release

- [ ] Set as latest release (if stable)
- [ ] Or mark as pre-release (if beta/RC)
- [ ] Click "Publish release"

---

## Post-Release Activities

### Immediately After Release

**1. Verify Downloads** (within 15 minutes)

```bash
# Test each download link
curl -I https://github.com/yourorg/shop-manager/releases/download/v1.1.0/Shop-Manager-Setup-1.1.0.exe

# Expected: HTTP 200 OK
```

**2. Update Website** (within 30 minutes)

```bash
# Update download links on shopmanager.com
# Update version number in hero section
# Publish blog post
```

**3. Social Media** (within 1 hour)

Post on all channels:

**Twitter**:
```
🎉 Shop Manager v1.1.0 is here!

✨ Email notifications
💰 Multi-currency support
⚡ 40% faster dashboard
🐛 Bug fixes & security updates

Download now: https://shopmanager.com/download

Full release notes: https://github.com/yourorg/shop-manager/releases/tag/v1.1.0

#retailtech #opensource #smallbusiness
```

**LinkedIn**:
```
We're excited to announce Shop Manager v1.1.0!

This release brings powerful new features to help small retail businesses:

📧 Email Notifications - Stay informed with automatic alerts for low stock, daily summaries, and more

💱 Multi-Currency Support - USD, EUR, GBP with automatic conversion

⚡ Performance - 40% faster dashboard loading and improved report generation

Plus critical bug fixes and security updates.

Download the free trial: https://shopmanager.com

#retailmanagement #smallbusiness #opensource
```

**4. Notify Users** (within 2 hours)

Send email to mailing list:

```
Subject: Shop Manager v1.1.0 Released - Email Notifications & More!

Hi there,

We're excited to announce Shop Manager v1.1.0 is now available!

🎉 What's New:
- Email notifications for low stock and daily summaries
- Multi-currency support (USD, EUR, GBP)
- 40% faster dashboard performance
- Critical bug fixes and security updates

📥 Download Now:
https://shopmanager.com/download

📖 Full Release Notes:
https://github.com/yourorg/shop-manager/releases/tag/v1.1.0

🔄 Upgrading:
If you're on v1.0.x, upgrading is seamless. See our upgrade guide:
https://shopmanager.com/docs/upgrade

Questions? Reply to this email or join our Discord: https://discord.gg/shopmanager

Happy selling!
The Shop Manager Team
```

### First Week After Release

**Monitor**:
- [ ] Download statistics (GitHub API)
- [ ] Error reports (GitHub Issues)
- [ ] Social media mentions
- [ ] Support requests
- [ ] Website traffic

**Engage**:
- [ ] Respond to all comments/questions
- [ ] Thank users for feedback
- [ ] Fix critical bugs immediately
- [ ] Update FAQ based on questions

**Analytics**:
```bash
# Get download count
gh api repos/yourorg/shop-manager/releases/latest \
  --jq '.assets[] | "\(.name): \(.download_count)"'

# Expected for first week:
# - 100-500 downloads (new projects)
# - 500-2000 downloads (established projects)
```

### First Month After Release

**Review**:
- [ ] Analyze adoption rate
- [ ] Review bug reports
- [ ] Gather user feedback
- [ ] Plan next release

**Content**:
- [ ] Publish case study
- [ ] Tutorial videos
- [ ] Blog post: "What we learned"
- [ ] Community highlights

---

## Hotfix Process

For critical bugs that need immediate attention:

### 1. Identify Issue

**Criteria for hotfix**:
- Data loss or corruption
- Security vulnerability
- Complete system failure
- Critical feature broken

**NOT a hotfix**:
- Minor UI glitches
- Nice-to-have features
- Non-critical bugs
- Performance tweaks

### 2. Create Hotfix Branch

```bash
# From main branch
git checkout main
git pull

# Create hotfix branch
git checkout -b hotfix/v1.0.2

# Fix the issue
# ... make changes ...

# Commit
git add .
git commit -m "fix: critical bug in sales calculation

Fixes issue where discounts were applied twice,
resulting in incorrect totals.

Resolves #123"
```

### 3. Test Thoroughly

```bash
# Run all tests
./mvnw verify

# Manual testing on all platforms
# Focus on the fix and related functionality
```

### 4. Version Bump

```bash
# Bump patch version
npm version 1.0.2 --no-git-tag-version

# Update CHANGELOG.md
```

### 5. Build and Release

```bash
# Build packages
cd standalone/scripts
./create-distribution.sh --version 1.0.2

# Create tag
git tag -a v1.0.2 -m "Hotfix: Critical bug in sales calculation"

# Push
git push origin hotfix/v1.0.2
git push origin v1.0.2

# Create GitHub release (mark as urgent)
gh release create v1.0.2 \
  --title "Shop Manager v1.0.2 (Hotfix)" \
  --notes "Critical security patch. All users should upgrade immediately." \
  dist/*
```

### 6. Merge Back

```bash
# Merge hotfix to main
git checkout main
git merge hotfix/v1.0.2
git push origin main

# Merge hotfix to develop (if using gitflow)
git checkout develop
git merge hotfix/v1.0.2
git push origin develop

# Delete hotfix branch
git branch -d hotfix/v1.0.2
git push origin --delete hotfix/v1.0.2
```

### 7. Notify Users ASAP

**Security Hotfix Email**:
```
Subject: [URGENT] Shop Manager v1.0.2 Security Patch

IMPORTANT: A critical security vulnerability has been discovered in Shop Manager v1.0.1 and earlier.

IMPACT:
[Describe the vulnerability without giving exploitation details]

ACTION REQUIRED:
Upgrade to v1.0.2 immediately: https://shopmanager.com/download

TIMELINE:
This hotfix was released on [date] at [time]. We discovered the issue on [date].

MORE INFO:
Full details: https://github.com/yourorg/shop-manager/security/advisories/GHSA-xxxx

Questions? Email security@shopmanager.com

Thank you for your immediate attention.
The Shop Manager Security Team
```

---

## Rollback Procedures

If a release has critical issues:

### 1. Assess Severity

**Rollback if**:
- Widespread data corruption
- Complete system failure
- Critical security issue introduced
- Majority of users cannot use the app

**Don't rollback if**:
- Minor bug affecting few users
- Workaround available
- Fix can be deployed quickly

### 2. Execute Rollback

**On GitHub**:
```bash
# Mark release as pre-release
gh release edit v1.1.0 --prerelease

# Or delete entirely (extreme)
gh release delete v1.1.0 --yes
git tag -d v1.1.0
git push origin :refs/tags/v1.1.0
```

**Update Website**:
```bash
# Revert download links to previous version
# Update homepage to show v1.0.1 as latest
# Add banner: "v1.1.0 temporarily unavailable"
```

### 3. Communicate Immediately

```
Subject: Shop Manager v1.1.0 Temporarily Withdrawn

We've temporarily withdrawn Shop Manager v1.1.0 due to a critical issue discovered after release.

WHAT HAPPENED:
[Brief explanation]

AFFECTED USERS:
If you upgraded to v1.1.0, please roll back to v1.0.1.

HOW TO ROLLBACK:
1. Stop Shop Manager
2. Download v1.0.1: [link]
3. Restore backup (if you made one)
4. Restart

NEXT STEPS:
We're working on a fix. v1.1.1 will be released within 48 hours.

STATUS UPDATES:
https://status.shopmanager.com

Apologies for the inconvenience.
The Shop Manager Team
```

### 4. Fix and Re-Release

```bash
# Fix the issue
git checkout -b fix/v1.1.1

# Make fixes
# Test extensively

# Bump to v1.1.1
# Release following normal process
```

---

## Release Cadence

### Ideal Schedule

**January** - v1.1.0 (minor)
- New features from Q4 development

**February** - Patches as needed

**March** - Patches as needed

**April** - v1.2.0 (minor)
- New features from Q1 development

**May** - Patches as needed

**June** - Patches as needed

**July** - v1.3.0 (minor)
- New features from Q2 development

**August** - Patches as needed

**September** - Patches as needed

**October** - v2.0.0 planning and beta

**November** - v2.0.0 RC testing

**December** - v2.0.0 (major) release

---

## Deprecation Policy

When removing features:

### 1. Announce Deprecation (Version N)

```markdown
## Deprecated in v1.5.0

The following features are deprecated and will be removed in v2.0.0:

- **Old API Endpoint** `/api/old/products`
  - Use `/api/v2/products` instead
  - Migration guide: [link]

- **Legacy Configuration** `config.legacy.yml`
  - Use `config.yaml` instead
  - Conversion tool: [link]
```

### 2. Warning Phase (Version N+1, N+2)

```javascript
// Show warnings in logs
console.warn("DEPRECATED: /api/old/products will be removed in v2.0.0. Use /api/v2/products instead.");
```

### 3. Removal (Version N+3 or Major)

```markdown
## Removed in v2.0.0

The following deprecated features have been removed:

- Old API endpoint `/api/old/products` (deprecated in v1.5.0)
- Legacy configuration format (deprecated in v1.5.0)

Migration guide: [link]
```

**Minimum Deprecation Period**:
- Minor features: 2 minor versions (e.g., deprecated in v1.5, removed in v1.7)
- Major features: 6 months or next major version

---

## Version Support Policy

**Latest Version**: Full support (bug fixes, features, security)

**Previous Minor Version**: Security patches only (e.g., if latest is v1.3.0, v1.2.x gets security patches)

**Older Versions**: No support (upgrade required)

**Example**:
- Latest: v1.3.0 (full support)
- v1.2.x: Security patches only
- v1.1.x and older: No support

**LTS (Long-Term Support)**:
For enterprise users, consider offering LTS versions:
- v1.0 LTS: Supported until Dec 2025
- v2.0 LTS: Supported until Dec 2026

---

## Release Automation

### GitHub Actions Workflow

`.github/workflows/release.yml`:

```yaml
name: Create Release

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Build packages
        run: |
          cd standalone/scripts
          ./create-distribution.sh --version ${GITHUB_REF#refs/tags/v}

      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: standalone/scripts/dist/*
          generate_release_notes: true
```

---

## Next Steps

After understanding the release process:
1. [Test Your Release](TESTING_GUIDE.md)
2. [Deploy to Channels](DEPLOYMENT_GUIDE.md)
3. [Launch Marketing](MARKETING_GUIDE.md)
4. [Set Up Support](CUSTOMER_SUPPORT.md)

---

## Support

For release-related questions:
- **Email**: releases@shopmanager.com
- **Slack**: #releases channel
- **Documentation**: https://docs.shopmanager.com/releases
