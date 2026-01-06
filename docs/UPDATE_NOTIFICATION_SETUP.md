# Shop Manager - Update Notification Setup

> **Status**: PLANNED - Not yet implemented
>
> This document describes the planned update notification system for Shop Manager embedded installations.

---

## Overview

The update notification system will allow embedded installations to receive notifications when new versions of Shop Manager are available.

### Planned Features

- **Automatic Version Checking**: Background service checks for updates every 24 hours
- **Frontend Notifications**: Banner notification when update available
- **Manual Check**: User-triggered update check from Settings
- **Release Notes**: View what's new in the latest version
- **Download Links**: Direct links to installers

---

## Planned Architecture

### Backend Components (To Be Implemented)

#### 1. UpdateCheckService
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/service/UpdateCheckService.java`

**Responsibilities**:
- Scheduled version check (daily)
- Call cloud API `/api/registration/latest-version`
- Compare with current version
- Cache result
- Emit event if update available

**Scheduled Task**:
```java
@Scheduled(cron = "0 0 */24 * * ?")  // Every 24 hours
public void checkForUpdates() {
    // Implementation pending
}
```

#### 2. UpdateController
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/controller/UpdateController.java`

**Planned Endpoints**:
```
GET  /api/updates/check          // Manual check
GET  /api/updates/status         // Get cached status
GET  /api/updates/release-notes  // Fetch release notes
```

### Cloud API Enhancement (To Be Implemented)

#### Latest Version Endpoint
**Location**: Aggregator module

**Endpoint**:
```
GET /api/registration/latest-version
Response: 200 OK
{
  "version": "0.1.28",
  "releaseDate": "2026-01-06",
  "downloadUrls": {
    "windows": "https://github.com/.../shop-manager-0.1.28-windows-x64-setup.exe",
    "macos": "https://github.com/.../shop-manager-0.1.28-macos-x64.dmg",
    "linux_deb": "https://github.com/.../shop-manager_0.1.28_all.deb",
    "linux_rpm": "https://github.com/.../shop-manager-0.1.28-1.x86_64.rpm"
  },
  "releaseNotes": "https://github.com/.../releases/tag/v0.1.28"
}
```

### Frontend Components (To Be Implemented)

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

## Implementation Roadmap

### Phase 1: Cloud API
- [ ] Add `/api/registration/latest-version` endpoint to AggregatorController
- [ ] Query GitHub Releases API or maintain version registry
- [ ] Return version, URLs, release notes

### Phase 2: Backend Service
- [ ] Create `UpdateCheckService` with scheduled task
- [ ] Create `UpdateController` with endpoints
- [ ] Cache update status in memory or database
- [ ] Unit tests

### Phase 3: Frontend UI
- [ ] Create `UpdateNotificationBanner` component
- [ ] Create `useUpdateCheck` hook
- [ ] Add notification to main layout
- [ ] Settings page integration (manual check button)

### Phase 4: Testing & Documentation
- [ ] Integration tests
- [ ] Update this documentation with implementation details
- [ ] User guide for update process

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

**Implemented**: None

**Next Steps**:
1. Decide on version source (GitHub API vs manual registry)
2. Implement cloud endpoint
3. Implement backend service
4. Implement frontend components
5. Update this document with implementation details

---

## Related Documentation

- [INSTALLER_FEATURES.md](./INSTALLER_FEATURES.md)
- [CLOUD_SYNC_ARCHITECTURE.md](./CLOUD_SYNC_ARCHITECTURE.md)
- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

---

**Last Updated**: 2026-01-06
**Status**: Planning Phase
