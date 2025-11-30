# Shop Manager - Distribution Guide

This document explains how to create and distribute Shop Manager packages for small businesses.

## 📦 Package Types

### 1. Docker Compose Bundle (Lightweight - 450 MB with images)

**Best for:** Technical users, developers, cloud deployments

**What's included:**
- `config.yaml` - Configuration file
- `install.sh` / `install.bat` - Automated installers
- `docker-compose.yml` - Service definitions
- `scripts/` - Configuration generator, certificate installer
- `templates/` - Keycloak realm template
- `docs/` - Installation and customization guides
- `docker-images/` (optional) - Pre-downloaded images for offline install

**Distribution methods:**
- Direct download (ZIP file from website)
- GitHub Releases
- Cloud storage (Google Drive, Dropbox)
- USB flash drive (offline installation)

**Installation time:** 15-20 minutes (online), 10-15 minutes (offline with pre-downloaded images)

### 2. Electron Desktop App (135 MB - Windows/macOS/Linux)

**Best for:** Non-technical users, small shops, easy deployment

**What's included:**
- Complete Electron application
- Visual configuration wizard
- Service management dashboard
- System tray integration
- Auto-updater
- Bundled Docker Compose files

**Distribution methods:**
- Direct download (`.exe`, `.dmg`, `.AppImage`)
- App stores (Mac App Store, Microsoft Store, Snap Store)
- Auto-update server

**Installation time:** 5-10 minutes (with Docker pre-installed)

### 3. Cloud Marketplace Images

**Best for:** Cloud-first users, no local installation needed

**Platforms:**
- **DigitalOcean 1-Click App:** $10/month (includes $5 server)
- **AWS Marketplace AMI:** Pay-as-you-go
- **Azure Marketplace:** Consumption-based pricing

**Installation time:** 5 minutes, fully managed

## 🚀 Creating Distribution Packages

### Create ZIP Bundle

```bash
cd standalone
./scripts/create-distribution.sh --version 1.0.0
```

This creates:
- `shop-manager-standalone-v1.0.0.zip` - Lightweight (online installation)
- `shop-manager-standalone-v1.0.0-full.zip` - With Docker images (offline)

### Build Electron Apps

```bash
cd standalone/electron-app

# Install dependencies
npm install

# Build for all platforms
npm run build:all

# Or build for specific platform
npm run build:win    # Windows
npm run build:mac    # macOS
npm run build:linux  # Linux
```

Output:
- Windows: `dist/Shop Manager-Setup-1.0.0.exe` (NSIS installer)
- macOS: `dist/Shop Manager-1.0.0.dmg` (DMG installer)
- Linux: `dist/Shop Manager-1.0.0.AppImage` (AppImage)

## 📋 Distribution Checklist

Before distributing a new release:

- [ ] Update version in `package.json`
- [ ] Update version in `config.yaml`
- [ ] Test installation on fresh Windows machine
- [ ] Test installation on fresh macOS machine
- [ ] Test installation on fresh Linux machine
- [ ] Verify all services start correctly
- [ ] Test configuration customization
- [ ] Test certificate generation
- [ ] Test backup/restore
- [ ] Update CHANGELOG.md
- [ ] Create GitHub release with binaries
- [ ] Update website download links
- [ ] Send release notes to mailing list

## 🔐 Code Signing (Production)

### Windows Code Signing

```bash
# Get a code signing certificate from a trusted CA
# (DigiCert, Sectigo, GlobalSign, etc.)

# Sign the executable
signtool sign /f certificate.pfx /p password /t http://timestamp.digicert.com Shop-Manager-Setup-1.0.0.exe
```

### macOS Code Signing

```bash
# Requires Apple Developer Account ($99/year)

# Sign the app
codesign --deep --force --verify --verbose --sign "Developer ID Application: Your Company" "Shop Manager.app"

# Notarize with Apple
xcrun notarytool submit "Shop Manager.dmg" --keychain-profile "AC_PASSWORD" --wait

# Staple the notarization
xcrun stapler staple "Shop Manager.dmg"
```

## 📊 Distribution Statistics to Track

- Total downloads
- Platform distribution (Windows/macOS/Linux)
- Installation success rate
- Average installation time
- Error rates during installation
- Active installations (telemetry with user consent)
- Upgrade rate

## 🌐 Hosting Options

### Static File Hosting

**Option 1: GitHub Releases** (Free)
- 2 GB per file limit
- Unlimited bandwidth
- Built-in version tracking
- CDN-backed

**Option 2: DigitalOcean Spaces** ($5/month)
- 250 GB storage
- 1 TB outbound transfer
- CDN included
- S3-compatible

**Option 3: AWS S3 + CloudFront**
- Pay-per-use
- Global CDN
- Auto-scaling

### Auto-Update Server

**Option 1: GitHub Releases** (Free)
- Electron auto-updater compatible
- No custom server needed

**Option 2: Self-hosted**
```javascript
// In electron-app/src/main.js
const { autoUpdater } = require('electron-updater');

autoUpdater.setFeedURL({
  provider: 'generic',
  url: 'https://releases.yourcompany.com/shop-manager/'
});

autoUpdater.checkForUpdatesAndNotify();
```

## 📱 Installation Methods Comparison

| Method | Ease of Use | Customization | Size | Best For |
|--------|-------------|---------------|------|----------|
| **Electron App** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 135 MB | Small shops, non-technical |
| **Docker Compose** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 450 MB | Developers, technical users |
| **USB Offline** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 2 GB | Offline areas, slow internet |
| **Cloud Marketplace** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | N/A | Cloud-first, managed hosting |

## 💰 Licensing Models

### Free Tier (Community Edition)
- Docker Compose bundle
- Community support (forums/GitHub)
- Manual updates
- Self-hosted only

### Small Business ($99 one-time)
- Electron desktop app
- Email support (48hr response)
- Free updates for 1 year
- 1 business license

### Professional ($299 one-time)
- Everything in Small Business
- Priority support (24hr response)
- Lifetime free updates
- 3 business licenses
- Remote installation assistance

### Cloud Hosted ($29/month)
- Fully managed
- Automatic updates
- Phone/chat support
- Unlimited users
- Daily backups

## 🎯 Target Customer Profiles

### Profile 1: Small Retail Shop (1-2 employees)
**Solution:** Electron Desktop App
**Why:** Easy installation, no technical knowledge required
**Price:** $99 one-time
**Support:** Email support

### Profile 2: Medium Business (5-10 shops)
**Solution:** Docker Compose on cloud VM
**Why:** Scalable, cost-effective, customizable
**Price:** $299 one-time + server costs
**Support:** Priority support

### Profile 3: Enterprise Retailer (50+ shops)
**Solution:** Kubernetes Helm chart
**Why:** Enterprise-grade, high availability, multi-region
**Price:** Custom licensing
**Support:** Dedicated account manager

### Profile 4: Franchise/Chain
**Solution:** Multi-tenant SaaS
**Why:** Centralized management, consistent experience
**Price:** $29/month per shop
**Support:** 24/7 phone support

## 📧 Marketing Distribution Channels

1. **Direct Website**
   - SEO-optimized product page
   - Free trial download
   - Live chat support

2. **App Marketplaces**
   - Mac App Store
   - Microsoft Store
   - Snap Store (Linux)

3. **Cloud Marketplaces**
   - AWS Marketplace
   - Azure Marketplace
   - DigitalOcean Marketplace

4. **Partner Channels**
   - Retail associations
   - POS hardware vendors
   - Accounting software integrations

5. **Affiliate Program**
   - IT consultants
   - Business advisors
   - 20% recurring commission

## 🔄 Update Strategy

### Major Versions (v1.x → v2.x)
- Paid upgrade for one-time licenses
- Included for cloud subscribers
- 3-month migration period
- Migration assistance

### Minor Versions (v1.0 → v1.1)
- Free for all users
- Auto-update for Electron apps
- Manual update for Docker Compose
- Release notes via email

### Patch Versions (v1.0.1 → v1.0.2)
- Immediate auto-update
- Security patches mandatory
- Silent update (background)

## 📞 Support Channels

### Community Support (Free)
- GitHub Issues
- Discord/Slack community
- Stack Overflow tag
- YouTube tutorials

### Email Support (Paid)
- support@shopmanager.com
- 48hr response time (Small Business)
- 24hr response time (Professional)
- 4hr response time (Enterprise)

### Phone Support (Enterprise)
- Dedicated hotline
- Business hours support
- 24/7 for critical issues

## 🎓 Documentation Strategy

**For End Users:**
- Quick Start Guide (2 pages)
- Video tutorials (YouTube)
- FAQs
- Troubleshooting guide

**For Developers:**
- API documentation (Swagger)
- Integration guides
- Webhook documentation
- Plugin development guide

**For IT Administrators:**
- Deployment guide
- Security best practices
- Backup/restore procedures
- Network requirements

## 🚀 Go-to-Market Timeline

**Month 1: Soft Launch**
- Release beta to 10 pilot customers
- Gather feedback
- Fix critical bugs
- Create documentation

**Month 2: Public Launch**
- Launch website
- Release to marketplaces
- Press release
- Social media campaign

**Month 3: Growth**
- Affiliate program
- Content marketing
- SEO optimization
- Customer case studies

**Month 4-6: Scale**
- Enterprise sales
- Partner integrations
- International expansion
- Feature enhancements

---

**Next Steps:**
1. Choose your target market
2. Select distribution method
3. Create packages
4. Set up hosting
5. Launch!

For questions: distribution@shopmanager.com
