# Shop Manager - Update Notification Setup

> **Status**: ✅ IMPLEMENTED
>
> This document describes the update notification system for Shop Manager embedded installations.

---

## Overview

The update notification system allows embedded installations to receive notifications when new versions of Shop Manager are available.

### Features

- **Automatic Version Checking**: Background service checks for updates every 24 hours
- **Frontend Notifications**: Banner notification when update available (pending)
- **Manual Check**: User-triggered update check from Settings
- **Release Notes**: View what's new in the latest version
- **Download Links**: Direct links to installers

---

## Architecture

### Backend Components

#### 1. UpdateCheckService ✅ IMPLEMENTED
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/service/UpdateCheckService.java`

**Responsibilities**:
- Scheduled version check (daily at midnight)
- Call cloud API `/api/registration/latest-version`
- Compare current version with latest using semantic versioning
- Cache result in memory
- Log update notifications

**Scheduled Task**:
```java
@Scheduled(cron = "${application.update-check.cron:0 0 */24 * * ?}")
public void scheduledUpdateCheck() {
    log.info("Running scheduled update check...");
    checkForUpdates();
}
```

**Configuration**:
- `application.update-check.enabled`: Enable/disable update checks (default: true)
- `application.update-check.cron`: Cron expression for schedule (default: daily at midnight)

#### 2. UpdateController ✅ IMPLEMENTED
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/controller/UpdateController.java`

**Endpoints**:
```
POST /api/updates/check    // Manual check (requires authentication)
GET  /api/updates/status   // Get cached status
```

**Authentication**: Requires `SYSTEM_ADMIN`, `TENANT_ADMIN`, `OWNER`, or `MANAGER` role

### Cloud API Enhancement ✅ IMPLEMENTED

#### Latest Version Endpoint
**Location**: `backend/src/main/java/com/princely/shopmanager/aggregator/controller/AggregatorController.java`

**Endpoint**: `GET /api/registration/latest-version`

**Response**:
```json
{
  "version": "0.1.29",
  "releaseDate": "2026-01-06",
  "downloadUrls": {
    "windows": "https://github.com/mabdulwasii/retail-manager/releases/download/v0.1.29/shop-manager-0.1.29-windows-x64-setup.exe",
    "macos": "https://github.com/mabdulwasii/retail-manager/releases/download/v0.1.29/shop-manager-0.1.29-macos-x64.dmg",
    "linux_deb": "https://github.com/mabdulwasii/retail-manager/releases/download/v0.1.29/shop-manager_0.1.29_all.deb",
    "linux_rpm": "https://github.com/mabdulwasii/retail-manager/releases/download/v0.1.29/shop-manager-0.1.29-1.x86_64.rpm",
    "linux_appimage": "https://github.com/mabdulwasii/retail-manager/releases/download/v0.1.29/shop-manager-0.1.29-x86_64.AppImage"
  },
  "releaseNotes": "https://github.com/mabdulwasii/retail-manager/releases/tag/v0.1.29"
}
```

**How it works**:
- Version is injected from `application.version` property (set via Maven `-Drevision`)
- Download URLs are dynamically constructed using GitHub releases pattern
- Cloud installation returns the version it's running

### Frontend Components (Planned for Future PR)

#### 1. UpdateNotificationBanner
**Location**: `frontend/src/components/UpdateNotificationBanner.tsx`

**Features**:
- Appears at top when update available
- Shows version number
- Buttons: "View Release Notes", "Download", "Dismiss"
- Dismissible (stores in localStorage)

#### 2. useUpdateCheck Hook
**Location**: `frontend/src/hooks/useUpdateCheck.ts`

**Responsibilities**:
- Poll `/api/updates/status` every 30 minutes
- Manage notification state
- Trigger manual check

---

## Configuration (Planned)

**.env Variables**:
```properties
# Update Notification Configuration
UPDATE_CHECK_ENABLED=true
UPDATE_CHECK_CRON=0 0 */24 * * ?  # Daily check
UPDATE_NOTIFICATION_DISMISS_DAYS=7  # Days before re-showing
```

---

## Implementation Status

### Phase 1: Cloud API ✅ COMPLETED
- [x] Add `/api/registration/latest-version` endpoint to AggregatorController
- [x] Return version, URLs, release notes dynamically

### Phase 2: Backend Service ✅ COMPLETED
- [x] Create `UpdateCheckService` with scheduled task
- [x] Create `UpdateController` with endpoints
- [x] Cache update status in memory (AtomicReference)
- [x] Add configuration properties to `application-embedded.yml`
- [x] Add environment variables to all installer `.env.template` files

### Phase 3: Frontend UI ✅ COMPLETED
- [x] Create `UpdateNotificationBanner` component
- [x] Create `useUpdateCheck` hook
- [x] Add notification to main layout
- [x] Create `updateService.ts` API service
- [x] Unit tests for all components
- [ ] Settings page integration (manual check button) - Optional future enhancement

### Phase 4: Testing & Documentation ✅ COMPLETED
- [x] Update this documentation with implementation details
- [x] Backend unit tests (18 tests total)
- [x] Frontend unit tests (9 tests for UpdateNotificationBanner)
- [ ] Integration tests (can be added later)
- [ ] User guide for update process (in INSTALLER_FEATURES.md)

---

## Alternative Approaches

### Option 1: GitHub Releases API (Current Plan)
- **Pros**: Simple, free, already hosting releases there
- **Cons**: Rate limiting, external dependency

### Option 2: Cloud Version Registry
- **Pros**: Full control, no rate limits
- **Cons**: Extra maintenance, manual version updates

### Option 3: No Auto-Check
- **Pros**: Simpler, less complexity
- **Cons**: Users miss updates, manual checking only

---

## Current Status

**Implementation**: ✅ COMPLETE (Backend + Frontend)

**What Works**:
- ✅ Scheduled update checks every 24 hours
- ✅ Manual update check via API
- ✅ Cached update status
- ✅ Semantic version comparison
- ✅ Cloud API endpoint for latest version
- ✅ Environment variable configuration
- ✅ Frontend notification banner
- ✅ Auto-polling every 30 minutes
- ✅ Dismissible notifications (7-day expiry)
- ✅ Platform-aware downloads
- ✅ Comprehensive unit tests (27 total)

**Optional Future Enhancements**:
- Settings page manual check button
- Integration tests
- User guide documentation

---

## Related Documentation

- [INSTALLER_FEATURES.md](./INSTALLER_FEATURES.md)
- [CLOUD_SYNC_ARCHITECTURE.md](./CLOUD_SYNC_ARCHITECTURE.md)
- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

---

**Last Updated**: 2026-01-06
**Status**: ✅ FULLY IMPLEMENTED (Backend + Frontend + Tests)
