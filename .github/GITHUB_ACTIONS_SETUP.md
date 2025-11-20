# GitHub Actions Setup Guide

This document explains how to configure GitHub Actions secrets for automated builds and deployments using **independent backend and frontend versioning**.

## Required Secrets

### 1. DOCKER_PASSWORD

**Purpose**: Authenticate with Docker Hub to push images

**How to create**:
1. Go to https://hub.docker.com/settings/security
2. Click **New Access Token**
3. Give it a name (e.g., "GitHub Actions")
4. Set permissions: **Read, Write, Delete**
5. Click **Generate**
6. **Copy the token** (you won't see it again)

**How to add to GitHub**:
1. Go to your repository: https://github.com/mabdulwasii/retail-manager
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `DOCKER_PASSWORD`
5. Value: Paste your Docker Hub access token
6. Click **Add secret**

---

## Workflows Overview

### build-backend.yml
- **Trigger**: Tags like `backend-v0.0.46`, `backend-v0.0.47`, etc.
- **Purpose**: Build multi-platform backend Docker image
- **Duration**: ~10 minutes
- **Required secrets**: `DOCKER_PASSWORD`
- **Output**:
  - `princely/shop-manager:backend-v{version}`
  - `princely/shop-manager:backend-latest`

### build-frontend.yml
- **Trigger**: Tags like `frontend-v0.0.10`, `frontend-v0.0.11`, etc.
- **Purpose**: Build multi-platform frontend Docker image
- **Duration**: ~5 minutes
- **Required secrets**: `DOCKER_PASSWORD`
- **Output**:
  - `princely/shop-manager:frontend-v{version}`
  - `princely/shop-manager:frontend-latest`

### publish-helm-chart.yml
- **Trigger**: Tags like `v0.0.46`, `v0.0.47`, etc. (unified version)
- **Purpose**: Package and publish Helm chart to Docker Hub OCI registry
- **Required secrets**: `DOCKER_PASSWORD`
- **Output**: `oci://registry-1.docker.io/princely/shop-manager:{version}`

---

## Tagging Strategy

### Independent Versioning

Backend and frontend have **separate version numbers** and release independently:

- **Backend**: `backend-v0.0.46` (current)
- **Frontend**: `frontend-v0.0.10` (current)
- **Helm Chart**: `v0.0.46` (unified version)

### When to Tag

#### Backend Changes Only
```bash
# Backend code changed, frontend unchanged
git tag -a backend-v0.0.47 -m "Backend: Add payment gateway integration"
git push origin backend-v0.0.47
```
**Result**: ✅ Builds backend only (~10 min)

#### Frontend Changes Only
```bash
# Frontend code changed, backend unchanged
git tag -a frontend-v0.0.11 -m "Frontend: Update dashboard UI"
git push origin frontend-v0.0.11
```
**Result**: ✅ Builds frontend only (~5 min)

#### Both Changed
```bash
# Both backend and frontend changed
git tag -a backend-v0.0.47 -m "Backend: New API endpoints"
git tag -a frontend-v0.0.11 -m "Frontend: Consume new APIs"
git push origin backend-v0.0.47 frontend-v0.0.11
```
**Result**: ✅ Builds both in parallel (~10 min total)

#### Helm Chart Updates
```bash
# Update Helm chart (after backend/frontend releases)
git tag -a v0.0.47 -m "Release v0.0.47"
git push origin v0.0.47
```
**Result**: ✅ Publishes Helm chart with latest backend/frontend versions

---

## Triggering Workflows

### Automatic (via Git Tags)

**Example 1: Backend Hotfix**
```bash
# Fix critical backend bug
git commit -m "fix: resolve authentication issue"
git tag -a backend-v0.0.47 -m "Backend: Fix auth bug"
git push origin backend-v0.0.47
```

**Example 2: Frontend Feature**
```bash
# Add new frontend dashboard
git commit -m "feat: add sales analytics dashboard"
git tag -a frontend-v0.0.11 -m "Frontend: Add analytics dashboard"
git push origin frontend-v0.0.11
```

**Example 3: Major Release**
```bash
# Both components updated for new release
git commit -m "feat: implement multi-currency support"

# Tag backend
git tag -a backend-v0.0.47 -m "Backend: Multi-currency API"

# Tag frontend
git tag -a frontend-v0.0.11 -m "Frontend: Multi-currency UI"

# Tag Helm chart
git tag -a v0.0.47 -m "Release v0.0.47: Multi-currency support"

# Push all tags
git push origin backend-v0.0.47 frontend-v0.0.11 v0.0.47
```

### Manual (via GitHub UI)

**Trigger Backend Build**:
1. Go to **Actions** → **Build and Push Backend (Multi-Platform)**
2. Click **Run workflow**
3. Enter:
   - **version**: `0.0.47` (without backend-v prefix)
   - **push_latest**: Check to also update `backend-latest` tag
4. Click **Run workflow**

**Trigger Frontend Build**:
1. Go to **Actions** → **Build and Push Frontend (Multi-Platform)**
2. Click **Run workflow**
3. Enter:
   - **version**: `0.0.11` (without frontend-v prefix)
   - **push_latest**: Check to also update `frontend-latest` tag
4. Click **Run workflow**

---

## Verifying Workflow Success

### Check GitHub Actions
Visit: https://github.com/mabdulwasii/retail-manager/actions

Look for:
- ✅ **Green checkmark** = Success
- ❌ **Red X** = Failed (click for logs)
- 🟡 **Yellow dot** = Running

### Check Docker Hub
Visit: https://hub.docker.com/r/princely/shop-manager/tags

You should see:
- `backend-v0.0.46`, `backend-v0.0.47`, etc.
- `frontend-v0.0.10`, `frontend-v0.0.11`, etc.
- `backend-latest` (points to newest backend)
- `frontend-latest` (points to newest frontend)

### Check Multi-Platform Support
```bash
docker manifest inspect princely/shop-manager:backend-v0.0.46
```

Should show:
- `linux/amd64`
- `linux/arm64`
- `windows/amd64`

### Check Helm Chart
```bash
helm show chart oci://registry-1.docker.io/princely/shop-manager --version 0.0.46
```

---

## Troubleshooting

### "Password required" error
**Cause**: `DOCKER_PASSWORD` secret is missing or incorrect
**Fix**: Follow steps above to create and add the secret

### Helm lint failures
**Cause**: Template syntax errors
**Fix**: Run `helm lint ./helm-chart/shop-manager` locally before pushing

### Build failures
**Cause**: Backend or frontend code doesn't compile
**Fix**: Test builds locally:
```bash
# Backend
cd backend && ./mvnw clean package

# Frontend
cd frontend && npm install && npm run build
```

### Wrong version in Docker tag
**Cause**: Tag format incorrect
**Fix**: Use exact format:
- Backend: `backend-v0.0.47` (not `v0.0.47-backend`)
- Frontend: `frontend-v0.0.11` (not `v0.0.11-frontend`)

---

## Version Management Best Practices

### Version Numbering

**Backend** (API changes):
- **Major** (v1.0.0): Breaking API changes
- **Minor** (v0.1.0): New features, backwards compatible
- **Patch** (v0.0.1): Bug fixes

**Frontend** (UI changes):
- **Major** (v1.0.0): Complete UI redesign
- **Minor** (v0.1.0): New pages/features
- **Patch** (v0.0.1): Bug fixes, small UI tweaks

**Helm Chart** (deployment changes):
- Match backend version for major releases
- Update when chart templates change

### Recommended Workflow

1. **Develop**: Make changes in feature branch
2. **Test**: Ensure tests pass locally
3. **Commit**: Commit with conventional commits (`feat:`, `fix:`)
4. **Merge**: Merge to main branch
5. **Tag**: Create appropriate version tag(s)
6. **Push**: Push tags to trigger builds
7. **Verify**: Check GitHub Actions and Docker Hub
8. **Deploy**: Update Helm chart if needed

---

## Security Best Practices

1. **Use access tokens, not passwords** for Docker Hub
2. **Rotate tokens regularly** (every 90 days recommended)
3. **Limit token scope** to only what's needed
4. **Never commit secrets** to the repository
5. **Use organization secrets** for shared tokens across repos
6. **Review workflow logs** for exposed secrets
7. **Enable branch protection** on main branch

---

## Examples

### Scenario 1: Backend Hotfix (No Frontend Changes)
```bash
# Fix critical backend security bug
git checkout main
git pull

# Make fix
vim backend/src/main/java/SecurityController.java
git commit -m "fix: patch security vulnerability CVE-2024-XXXX"

# Tag backend only
git tag -a backend-v0.0.47 -m "Backend: Security hotfix"
git push origin main backend-v0.0.47
```
**Result**: Only backend rebuilds (~10 min), frontend unchanged

### Scenario 2: Frontend UI Update (No Backend Changes)
```bash
# Update dashboard styling
git checkout main
git pull

# Make changes
vim frontend/src/components/Dashboard.tsx
git commit -m "feat: improve dashboard UX"

# Tag frontend only
git tag -a frontend-v0.0.11 -m "Frontend: Dashboard improvements"
git push origin main frontend-v0.0.11
```
**Result**: Only frontend rebuilds (~5 min), backend unchanged

### Scenario 3: Major Feature Release (Both Changed)
```bash
# Implement new reporting feature
git checkout main
git pull

# Backend API
git commit -m "feat: add reporting API endpoints"

# Frontend UI
git commit -m "feat: add reporting dashboard"

# Tag both components
git tag -a backend-v0.0.47 -m "Backend: Reporting API"
git tag -a frontend-v0.0.11 -m "Frontend: Reporting UI"

# Tag Helm chart
git tag -a v0.0.47 -m "Release v0.0.47: Reporting feature"

# Push everything
git push origin main backend-v0.0.47 frontend-v0.0.11 v0.0.47
```
**Result**: Both rebuild in parallel (~10 min), Helm chart published

---

## Support

- **Workflow issues**: Check the [Actions tab](https://github.com/mabdulwasii/retail-manager/actions)
- **Docker Hub issues**: https://hub.docker.com/support
- **Repository issues**: https://github.com/mabdulwasii/retail-manager/issues

---

## Quick Reference

| Task | Command |
|------|---------|
| Tag backend | `git tag -a backend-v0.0.X -m "message"` |
| Tag frontend | `git tag -a frontend-v0.0.X -m "message"` |
| Tag Helm chart | `git tag -a v0.0.X -m "message"` |
| Push tags | `git push origin tag-name` |
| View local tags | `git tag -l` |
| Delete local tag | `git tag -d tag-name` |
| Delete remote tag | `git push origin :refs/tags/tag-name` |
| View tag details | `git show tag-name` |

---

**Updated**: November 2024
**Strategy**: Independent backend/frontend versioning for faster, more flexible releases
