# Shop Manager - Deployment Guide

This guide covers deploying Shop Manager packages to various distribution channels.

## Table of Contents

1. [Deploying Marketing Landing Page](#deploying-marketing-landing-page)
2. [GitHub Releases](#github-releases)
3. [App Store Submissions](#app-store-submissions)
4. [Cloud Marketplaces](#cloud-marketplaces)
5. [CDN Setup](#cdn-setup)
6. [Custom Domain](#custom-domain)

---

## Deploying Marketing Landing Page

### Option 1: Netlify (Recommended - Easiest)

**1. Sign up at netlify.com**

**2. Deploy via Git:**
```bash
# Push your code to GitHub
git add standalone/marketing/*
git commit -m "Add landing page"
git push origin main

# On Netlify dashboard:
# 1. Click "Add new site" → "Import an existing project"
# 2. Connect to GitHub
# 3. Select repository
# 4. Build settings:
#    - Base directory: standalone/marketing
#    - Build command: (leave empty)
#    - Publish directory: .
# 5. Click "Deploy"
```

**3. Custom domain:**
```
# In Netlify dashboard:
# Site settings → Domain management → Add custom domain
# Add: shopmanager.com
# Follow DNS instructions
```

**Cost:** Free (100GB bandwidth/month)

**Deploy time:** 2-3 minutes

**URL:** `https://your-site.netlify.app` or custom domain

---

### Option 2: Vercel

**1. Install Vercel CLI:**
```bash
npm install -g vercel
```

**2. Deploy:**
```bash
cd standalone/marketing
vercel

# Follow prompts:
# - Set up and deploy: Y
# - Which scope: (your account)
# - Link to existing project: N
# - Project name: shop-manager
# - Directory: ./
# - Override settings: N
```

**3. Production deployment:**
```bash
vercel --prod
```

**Custom domain:**
```bash
vercel domains add shopmanager.com
# Follow DNS instructions
```

**Cost:** Free (100GB bandwidth)

---

### Option 3: GitHub Pages

**1. Create gh-pages branch:**
```bash
git checkout -b gh-pages
git push origin gh-pages
```

**2. Enable GitHub Pages:**
```
# Repository → Settings → Pages
# Source: gh-pages branch
# Folder: / (root)
# Save
```

**3. Custom domain:**
```
# Settings → Pages → Custom domain
# Add: shopmanager.com
# Create CNAME file:
echo "shopmanager.com" > standalone/marketing/CNAME
```

**Cost:** Free

**URL:** `https://yourusername.github.io/shop-manager`

---

### Option 4: AWS S3 + CloudFront

**1. Create S3 bucket:**
```bash
aws s3 mb s3://shopmanager.com
aws s3 sync standalone/marketing/ s3://shopmanager.com/
```

**2. Enable static website hosting:**
```bash
aws s3 website s3://shopmanager.com/ \
  --index-document index.html \
  --error-document index.html
```

**3. Create CloudFront distribution:**
```bash
aws cloudfront create-distribution \
  --origin-domain-name shopmanager.com.s3.amazonaws.com \
  --default-root-object index.html
```

**Cost:** ~$1-5/month + data transfer

---

## GitHub Releases

### Automated Release (Recommended)

**1. Tag version:**
```bash
# Ensure all changes are committed
git add .
git commit -m "Release v1.0.0"

# Create and push tag
git tag v1.0.0
git push origin v1.0.0
```

**2. GitHub Actions automatically:**
- Builds all platform packages
- Creates checksums
- Generates release notes
- Uploads artifacts
- Publishes release

**3. Monitor build:**
```
# Visit: https://github.com/yourorg/shop-manager/actions
# Watch the build progress (15-30 minutes)
```

**4. Edit release notes:**
```
# When build completes:
# Go to: https://github.com/yourorg/shop-manager/releases
# Click "Edit" on draft release
# Customize release notes
# Click "Publish release"
```

---

### Manual Release

**1. Create release:**
```bash
# Install GitHub CLI
brew install gh  # macOS
# OR
sudo apt install gh  # Ubuntu

# Create release
gh release create v1.0.0 \
  --title "Shop Manager v1.0.0" \
  --notes "See CHANGELOG.md for details"
```

**2. Upload artifacts:**
```bash
cd standalone/scripts/dist

# Upload all packages
gh release upload v1.0.0 *.zip *.exe *.dmg *.AppImage *.deb *.rpm

# Upload checksums
gh release upload v1.0.0 *.sha256 *.md5
```

**3. Publish:**
```bash
# Mark as latest
gh release edit v1.0.0 --latest

# Or mark as pre-release
gh release edit v1.0.0 --prerelease
```

---

## App Store Submissions

### Mac App Store

**Prerequisites:**
- Apple Developer account ($99/year)
- App Store Connect access
- App-specific password
- Provisioning profiles

**1. Prepare app:**
```bash
cd standalone/electron-app

# Update version
npm version 1.0.0

# Build for Mac App Store
npm run build:mac -- --mac mas
```

**2. Create App Store listing:**
```
# Visit: https://appstoreconnect.apple.com
# My Apps → + → New App
# Fill in:
# - Name: Shop Manager
# - Primary Language: English
# - Bundle ID: com.yourcompany.shopmanager
# - SKU: shopmanager-1.0.0
```

**3. Upload binary:**
```bash
# Using Transporter app (download from App Store)
# Or using xcrun:
xcrun altool --upload-app \
  --type osx \
  --file "Shop Manager-1.0.0.pkg" \
  --username "your@email.com" \
  --password "app-specific-password"
```

**4. Submit for review:**
```
# App Store Connect → My Apps → Shop Manager
# + Version or Platform → macOS
# Fill in metadata, screenshots, descriptions
# Submit for Review
```

**Review time:** 1-3 days

---

### Microsoft Store

**Prerequisites:**
- Microsoft Partner Center account (free)
- Windows Developer account ($19 one-time)

**1. Create app reservation:**
```
# Visit: https://partner.microsoft.com/dashboard
# Apps and games → New product → App
# Reserve name: Shop Manager
```

**2. Prepare package:**
```bash
# Install Windows SDK
# Build APPX package
cd standalone/electron-app
npm run build:win -- --win appx
```

**3. Upload package:**
```
# Partner Center → Shop Manager → Packages
# Upload APPX file
# Fill in:
# - Product description
# - Screenshots (1366x768, 1920x1080)
# - Privacy policy URL
# - Age ratings
```

**4. Submit:**
```
# Review and submit
# Certification time: 1-3 days
```

---

### Snap Store (Linux)

**Prerequisites:**
- Ubuntu One account (free)
- snapcraft installed

**1. Create snap:**
```bash
cd standalone/electron-app

# Create snapcraft.yaml
cat > snapcraft.yaml <<EOF
name: shop-manager
version: '1.0.0'
summary: Retail Management Made Simple
description: |
  Complete point-of-sale and inventory management system
  for small retail businesses.

grade: stable
confinement: strict
base: core22

apps:
  shop-manager:
    command: shop-manager
    plugs: [home, network, x11]

parts:
  shop-manager:
    plugin: nil
    override-build: |
      cp -r dist/linux-unpacked/* $SNAPCRAFT_PART_INSTALL/
EOF

# Build snap
snapcraft
```

**2. Test snap:**
```bash
sudo snap install --dangerous shop-manager_1.0.0_amd64.snap
shop-manager
```

**3. Upload to Snap Store:**
```bash
# Login
snapcraft login

# Upload
snapcraft upload shop-manager_1.0.0_amd64.snap --release stable
```

**Review time:** Automated (instant for established developers)

---

## Cloud Marketplaces

### DigitalOcean Marketplace

**1. Create 1-Click App:**
```bash
# Create Droplet snapshot with Shop Manager installed
# Requirements:
# - Clean Ubuntu 22.04 LTS
# - Shop Manager installed and configured
# - Documentation in /opt/shop-manager/README.md
```

**2. Submit application:**
```
# Visit: https://cloud.digitalocean.com/vendorportal
# Submit 1-Click App
# Provide:
# - App name: Shop Manager
# - Category: Business & Commerce
# - Snapshot ID
# - Installation instructions
# - Support URL
```

**Review time:** 2-4 weeks

**Revenue share:** 25% to DigitalOcean

---

### AWS Marketplace

**1. Create AMI:**
```bash
# Launch EC2 instance
# Install Shop Manager
# Create AMI

aws ec2 create-image \
  --instance-id i-1234567890abcdef0 \
  --name "Shop Manager 1.0.0" \
  --description "Retail management system"
```

**2. Register as seller:**
```
# Visit: https://aws.amazon.com/marketplace/management
# Register as seller (requires tax info)
```

**3. Create product:**
```
# AWS Marketplace Management Portal
# Products → Add new product → AMI
# Fill in product details
# Upload AMI
# Set pricing (free, hourly, monthly)
```

**Review time:** 2-6 weeks

---

### Azure Marketplace

**1. Create VM image:**
```bash
# Create Azure VM
# Install Shop Manager
# Generalize VM

az vm deallocate --resource-group myResourceGroup --name myVM
az vm generalize --resource-group myResourceGroup --name myVM
az image create --resource-group myResourceGroup --name shop-manager-1.0.0 --source myVM
```

**2. Partner Center:**
```
# Visit: https://partner.microsoft.com/dashboard/commercial-marketplace
# New offer → Azure Virtual Machine
# Fill in details
# Upload image
```

**Review time:** 3-5 weeks

---

## CDN Setup

### Cloudflare (Recommended)

**1. Sign up at cloudflare.com**

**2. Add domain:**
```
# Add site → Enter domain → Select plan (Free)
# Update nameservers at your domain registrar
```

**3. Configure caching:**
```
# Rules → Page Rules → Create Page Rule
# URL: shopmanager.com/download/*
# Settings:
#   - Cache Level: Cache Everything
#   - Edge Cache TTL: 1 month
```

**4. Enable optimizations:**
```
# Speed → Optimization
# ✓ Auto Minify (HTML, CSS, JS)
# ✓ Brotli
# ✓ Rocket Loader
```

**Cost:** Free (unlimited bandwidth)

---

### Amazon CloudFront

**1. Create distribution:**
```bash
aws cloudfront create-distribution \
  --origin-domain-name s3://your-bucket.s3.amazonaws.com \
  --default-cache-behavior \
    "TargetOriginId=S3-shop-manager,ViewerProtocolPolicy=redirect-to-https,MinTTL=0,AllowedMethods=GET,HEAD,Compress=true"
```

**2. Configure caching:**
```
# Set cache policy for downloads
# Minimum TTL: 86400 (1 day)
# Maximum TTL: 31536000 (1 year)
```

**Cost:** $0.085/GB (first 10TB)

---

## Custom Domain

### DNS Configuration

**For landing page (Netlify/Vercel):**
```
# Add A records:
A    @    <netlify-ip>
CNAME www shopmanager.netlify.app

# Or for Vercel:
CNAME @ cname.vercel-dns.com
CNAME www cname.vercel-dns.com
```

**For downloads (CDN):**
```
# Add CNAME for download subdomain:
CNAME downloads shopmanager.b-cdn.net

# Usage: https://downloads.shopmanager.com/v1.0.0/shop-manager.zip
```

### SSL Certificate

**Automatic (Netlify/Vercel/Cloudflare):**
- Automatically provisioned
- Free Let's Encrypt certificate
- Auto-renewal

**Manual (if needed):**
```bash
# Using Let's Encrypt
sudo certbot certonly --standalone -d shopmanager.com -d www.shopmanager.com

# Certificate locations:
# /etc/letsencrypt/live/shopmanager.com/fullchain.pem
# /etc/letsencrypt/live/shopmanager.com/privkey.pem
```

---

## Deployment Checklist

Before deploying to production:

**Landing Page:**
- [ ] All links work
- [ ] Download buttons point to correct files
- [ ] Forms submit correctly (if any)
- [ ] Mobile responsive
- [ ] Fast load time (<3s)
- [ ] SEO optimized (meta tags, sitemap)
- [ ] Analytics integrated
- [ ] SSL certificate installed
- [ ] Custom domain configured

**GitHub Releases:**
- [ ] All packages uploaded
- [ ] Checksums verified
- [ ] Release notes complete
- [ ] Version tagged correctly
- [ ] Latest release marked
- [ ] Pre-release if needed

**App Stores:**
- [ ] Screenshots prepared (required sizes)
- [ ] App description written
- [ ] Privacy policy URL
- [ ] Support URL
- [ ] Age rating completed
- [ ] Pricing set
- [ ] In-app purchases configured (if any)

**CDN:**
- [ ] Origin configured
- [ ] Caching rules set
- [ ] Compression enabled
- [ ] HTTPS enforced
- [ ] Custom domain working
- [ ] Invalidation tested

---

## Monitoring Deployments

### Check Landing Page

```bash
# Test load time
curl -w "@curl-format.txt" -o /dev/null -s https://shopmanager.com

# Check SSL
openssl s_client -connect shopmanager.com:443 -servername shopmanager.com

# Validate HTML
curl https://shopmanager.com | tidy -q -e
```

### Monitor GitHub Release Downloads

```bash
# Get download stats
gh api repos/yourorg/shop-manager/releases/latest \
  --jq '.assets[] | "\(.name): \(.download_count)"'
```

### CDN Analytics

**Cloudflare:**
```
# Dashboard → Analytics → Traffic
# View: Bandwidth, Requests, Cache ratio
```

**CloudFront:**
```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/CloudFront \
  --metric-name Requests \
  --dimensions Name=DistributionId,Value=E1234567890ABC \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-31T23:59:59Z \
  --period 86400 \
  --statistics Sum
```

---

## Rollback Procedures

### Landing Page Rollback

**Netlify:**
```
# Dashboard → Deploys → Click previous deploy → Publish
```

**Vercel:**
```bash
vercel rollback
```

**GitHub Pages:**
```bash
git revert HEAD
git push origin gh-pages
```

### GitHub Release Rollback

```bash
# Delete release
gh release delete v1.0.0

# Delete tag
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0

# Re-create with fixes
git tag v1.0.0
git push origin v1.0.0
```

---

## Next Steps

After deployment:
1. [Set up analytics](ANALYTICS_TRACKING.md)
2. [Monitor for issues](CUSTOMER_SUPPORT.md)
3. [Plan marketing launch](MARKETING_GUIDE.md)

---

## Support

- **Deployment Issues:** ops@shopmanager.com
- **Slack:** #deployments channel
- **Documentation:** https://docs.shopmanager.com/deploy
