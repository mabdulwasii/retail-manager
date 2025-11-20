# GitHub Actions Setup Guide

This document explains how to configure GitHub Actions secrets for automated builds and deployments.

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

## Workflows Using These Secrets

### build-and-push-images.yml
- **Trigger**: Push tags like `v0.0.47`, or manual workflow dispatch
- **Purpose**: Build multi-platform Docker images (Linux amd64/arm64, Windows amd64)
- **Required secrets**: `DOCKER_PASSWORD`
- **Output**:
  - `princely/shop-manager:backend-v{version}`
  - `princely/shop-manager:frontend-v{version}`
  - `princely/shop-manager:backend-latest` (if triggered from main)
  - `princely/shop-manager:frontend-latest` (if triggered from main)

### publish-helm-chart.yml
- **Trigger**: Push tags like `v0.0.47`, or manual workflow dispatch
- **Purpose**: Package and publish Helm chart to Docker Hub OCI registry
- **Required secrets**: `DOCKER_PASSWORD`
- **Output**: `oci://registry-1.docker.io/princely/shop-manager:{version}`

## Triggering Workflows

### Automatic (via Git Tag)
```bash
# Create and push a version tag
git tag -a v0.0.47 -m "Release v0.0.47"
git push origin v0.0.47
```

This will automatically:
1. Build multi-platform Docker images
2. Publish Helm chart
3. Create GitHub Release

### Manual (via GitHub UI)
1. Go to **Actions** tab
2. Select the workflow (e.g., "Build and Push Multi-Platform Docker Images")
3. Click **Run workflow**
4. Enter parameters:
   - **version**: e.g., `0.0.47`
   - **push_latest**: Check to also tag as latest
5. Click **Run workflow**

## Verifying Workflow Success

### Check Docker Hub
Visit: https://hub.docker.com/r/princely/shop-manager/tags

You should see:
- `backend-v0.0.47`
- `frontend-v0.0.47`
- `backend-latest`
- `frontend-latest`

### Check Helm Chart
```bash
helm show chart oci://registry-1.docker.io/princely/shop-manager --version 0.0.47
```

### Check GitHub Releases
Visit: https://github.com/mabdulwasii/retail-manager/releases

## Troubleshooting

### "Password required" error
- **Cause**: `DOCKER_PASSWORD` secret is missing or incorrect
- **Fix**: Follow steps above to create and add the secret

### Helm lint failures
- **Cause**: Template syntax errors
- **Fix**: Run `helm lint ./helm-chart/shop-manager` locally before pushing

### Build failures
- **Cause**: Build errors in backend or frontend code
- **Fix**: Test builds locally:
  ```bash
  # Backend
  cd backend && ../mvnw clean package

  # Frontend
  cd frontend && npm install && npm run build
  ```

## Security Best Practices

1. **Use access tokens, not passwords** for Docker Hub
2. **Rotate tokens regularly** (every 90 days recommended)
3. **Limit token scope** to only what's needed (Read, Write for CI/CD)
4. **Never commit secrets** to the repository
5. **Use organization secrets** for shared tokens across multiple repos

## Additional Configuration

### Organization-Level Secrets
If you have multiple repositories needing the same Docker Hub credentials:

1. Go to organization settings: https://github.com/organizations/{org}/settings/secrets/actions
2. Add secrets at organization level
3. Select which repositories can access them

### Environment-Specific Secrets
For staging/production environments:

1. Repository **Settings** → **Environments**
2. Create environments (e.g., "production", "staging")
3. Add environment-specific secrets
4. Require approvals for production deploys

## Support

- **Workflow issues**: Check the Actions tab for error logs
- **Docker Hub issues**: https://hub.docker.com/support
- **Repository issues**: https://github.com/mabdulwasii/retail-manager/issues
