# Calendar-Driven Alarm App - Development Plan

## 📋 Project Overview

**Goal:** Build an Android app that syncs with the local calendar provider, detects structured alarm configuration blocks in event descriptions, and schedules precise device alarms at specified minute offsets before the event.

**PRD Reference:** See [`MVP PRD + Developer Checklist — Calendar-Driven Alarm App (Calendar → Device Alarms).md`](../documentation_development/MVP PRD + Developer Checklist — Calendar-Driven Alarm App (Calendar → Device Alarms).md)

**Chosen Architecture (MVP):** 
- **Calendar Access:** Android `CalendarContract` Provider (`READ_CALENDAR` + `WRITE_CALENDAR`)
- **Tech Stack:** Kotlin + MVVM + Jetpack Compose + Room + WorkManager + Hilt + Coroutines
- **Design Principles:** Idempotent reconciliation, self-documenting code, graceful degradation on restricted devices

**Timeline:** 6-8 weeks (MVP)

**Architecture Decision:** For the MVP we are using the native Android Calendar Provider instead of Google Calendar API + OAuth. This reduces complexity while fully supporting the core PRD requirements for alarm block parsing and reliable local alarm scheduling.

---

## 📋 Architecture Decision Record (ADR-001)

**Decision:** Use Android Calendar Provider (`CalendarContract`) for MVP  
**Date:** 2026-04-06  
**Status:** Approved  

**Rationale:** 
- Simpler implementation (no OAuth flow or Google Cloud project)
- Works with any locally synced calendar (Google, Outlook, etc.)
- Sufficient for core PRD functionality
- Faster path to validating the alarm block parsing + scheduling logic

**Alternatives Considered:** Google Calendar API (deferred to post-MVP)

---

## 🎯 Sprint Breakdown

### **Sprint 1: Foundation & Data Layer (Week 1-2)**

#### **Task 1.1: Project Setup & Dependencies**
**Assignee:** Lead Developer  
**Duration:** 2 days  
**Goal:** Establish project foundation with proper build configuration

**Deliverables:**
- Android Studio project with proper module structure
- `build.gradle.kts` with all required dependencies (Room, Hilt, Compose, WorkManager)
- Git repository setup with proper `.gitignore`
- Basic CI/CD pipeline configuration

**Acceptance Criteria:**
- Project builds successfully
- All team members can clone and run the project
- Dependency versions are aligned and compatible

---

#### **Task 1.2: Data Models & Database Schema**
**Assignee:** Lead Developer  
**Duration:** 3 days  
**Goal:** Define core data structures and local storage

**Deliverables:**
- `ScheduledAlarm` entity with Room annotations
- `EventSyncState` entity for tracking sync status
- `AlarmDatabase` class with proper migrations
- `AlarmDao` with all CRUD operations
- Database version management strategy

**Key Requirements:**
- Support for idempotent alarm operations
- Efficient querying by trigger time and event ID
- Proper indexing for performance
- Foreign key relationships where appropriate

**Acceptance Criteria:**
- Database creates successfully on first app launch
- All DAO operations work with test data
- Database inspector shows proper schema
- Unit tests pass for all DAO operations

---

#### **Task 1.3: Alarm Configuration Parser**
**Assignee:** Lead Developer  
**Duration:** 4 days  
**Goal:** Parse alarm configuration blocks from calendar event descriptions (must fully match PRD spec)

**Deliverables:**
- `AlarmConfig` data class
- `AlarmConfigParser` with robust parsing logic
- Support for the exact format specified in PRD:
  ```
  @alarmapp:v1
  @enabled:true
  @base:START
  @alarms:90,45,15
  @label:Meeting alarm
  ```
- Comprehensive error handling for malformed configs
- Unit tests covering all edge cases

**Edge Cases to Handle:**
- Missing header (`@alarmapp:v1`)
- Invalid offset values (non-integers, negatives)
- Duplicate offsets
- Unknown fields (forward compatibility)
- Empty or malformed alarm lists

**Acceptance Criteria:**
- Parser correctly identifies valid configuration blocks
- Invalid blocks are safely ignored without crashes
- All edge cases are handled gracefully
- 100% unit test coverage for parser logic

---

### **Sprint 2: Business Logic & Repository Layer (Week 2-3)**

#### **Task 2.1: Repository Pattern Implementation**
**Assignee:** Senior Android Developer  
**Duration:** 4 days  
**Goal:** Implement clean architecture with repository pattern

**Deliverables:**
- `AlarmRepository` interface defining all data operations
- `AlarmRepositoryImpl` with Room database integration
- `CalendarRepository` interface for calendar operations
- Proper error handling and result wrapping
- Repository unit tests with mocked dependencies

**Key Operations:**
- `getUpcomingAlarms(timeWindow: Long): List<ScheduledAlarm>`
- `scheduleAlarm(alarm: ScheduledAlarm): Result<Unit>`
- `cancelAlarm(alarmId: Long): Result<Unit>`
- `syncWithCalendar(): Result<SyncResult>`
- `reconcileAlarms(desired: List<AlarmTrigger>): Result<Unit>`

**Acceptance Criteria:**
- All repository operations are async (suspend functions)
- Proper error handling with sealed Result classes
- Repository tests achieve 90%+ coverage
- No direct database access outside repository layer

---

#### **Task 2.2: Calendar Integration**
**Assignee:** Lead Developer  
**Duration:** 5 days  
**Goal:** Integrate with Android Calendar Provider (`CalendarContract`)

**Deliverables:**
- `CalendarService` / `CalendarRepository` for reading events via `CalendarContract`
- Robust permission handling (`READ_CALENDAR`, optional `WRITE_CALENDAR`)
- Event filtering for next 24 hours (per PRD)
- Proper cursor management and timezone handling
- Graceful degradation when permission is denied

**Technical Requirements:**
- Use `CalendarContract.Instances` and `Events` tables
- Support recurring events by treating each instance separately
- Proper cursor management and resource cleanup
- Timezone-aware start time calculation
- Event description parsing for `@alarmapp:v1` blocks

**Acceptance Criteria:**
- Can read events from user's primary calendar
- Properly handles permission requests and denials
- Filters events to next 24-hour window
- Extracts event descriptions for alarm config parsing
- No memory leaks from cursor operations

---

#### **Task 2.3: Use Cases/Interactors**
**Assignee:** Lead Developer  
**Duration:** 4 days  
**Goal:** Implement business logic use cases following PRD reconciliation rules

**Deliverables:**
- `SyncAlarmsUseCase` - orchestrates calendar sync and alarm reconciliation
- `ScheduleAlarmUseCase` - handles individual alarm scheduling
- `CancelAlarmUseCase` - handles alarm cancellation
- `GetUpcomingAlarmsUseCase` - retrieves and formats alarm data for UI
- Proper dependency injection setup

**Business Rules to Implement:**
- Idempotent alarm scheduling (no duplicates)
- Automatic cleanup of past alarms
- Conflict resolution when events change
- Graceful degradation when calendar access is denied

**Acceptance Criteria:**
- All use cases are testable in isolation
- Business logic is separated from Android framework dependencies
- Error scenarios are properly handled
- Use cases integrate cleanly with repository layer

---

### **Sprint 3: Platform Integration & Background Processing (Week 3-4)**

#### **Task 3.1: Alarm Scheduling System**
**Assignee:** Senior Android Developer  
**Duration:** 4 days  
**Goal:** Implement precise alarm scheduling using Android AlarmManager

**Deliverables:**
- `AlarmScheduler` interface and implementation
- `AlarmReceiver` BroadcastReceiver for handling alarm triggers
- Support for exact alarms with `SCHEDULE_EXACT_ALARM` permission
- Fallback strategy for devices that don't support exact alarms
- Alarm notification system with proper channels

**Technical Challenges:**
- Android 12+ exact alarm permission handling
- Battery optimization whitelist guidance
- Different OEM behavior handling
- Alarm limit management (Android has limits on pending alarms)

**Acceptance Criteria:**
- Alarms trigger at precise times (within 1-2 seconds)
- Proper permission handling for exact alarms
- Fallback behavior works on restricted devices
- Alarm notifications are attention-grabbing but not annoying
- No alarm duplication or orphaned alarms

---

#### **Task 3.2: Background Sync with WorkManager**
**Assignee:** Android Developer with WorkManager experience  
**Duration:** 4 days  
**Goal:** Implement reliable background synchronization

**Deliverables:**
- `SyncWorker` for periodic calendar synchronization
- `SyncWorkManager` for scheduling and managing sync work
- Proper work constraints (network, battery, etc.)
- Exponential backoff for failed syncs
- Sync status reporting to UI

**Sync Strategy:**
- Periodic sync every 15 minutes (when possible)
- Immediate sync on app foreground
- Manual sync trigger from UI
- Adaptive sync frequency based on success/failure rates

**Acceptance Criteria:**
- Background sync works reliably across different Android versions
- Proper handling of Doze mode and App Standby
- Sync failures don't crash the app
- Users can see last sync time and status
- Battery usage is reasonable

---

#### **Task 3.3: Permission Management**
**Assignee:** Lead Developer  
**Duration:** 3 days  
**Goal:** Handle all required permissions gracefully

**Deliverables:**
- `PermissionManager` for centralized permission handling
- User-friendly permission request flows
- Graceful degradation when permissions are denied
- Settings deep-link for manual permission enabling
- Clear user education about why permissions are needed

**Required Permissions:**
- `READ_CALENDAR` - for reading calendar events
- `WRITE_CALENDAR` - for updating event descriptions (optional)
- `SCHEDULE_EXACT_ALARM` - for precise alarm scheduling (Android 12+)
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - for reliable background sync

**Acceptance Criteria:**
- Permission requests are contextual and well-explained
- App functions reasonably even with denied permissions
- Users can easily enable permissions from settings
- No permission-related crashes

---

### **Sprint 4: UI Layer with Jetpack Compose (Week 4-5)**

#### **Task 4.1: ViewModels with StateFlow**
**Assignee:** Lead Developer  
**Duration:** 3 days  
**Goal:** Implement MVVM ViewModels with modern state management

**Deliverables:**
- `AlarmListViewModel` - manages alarm list state and operations
- `SettingsViewModel` - handles app configuration
- `SyncStatusViewModel` - tracks sync status and manual triggers
- Proper state management with `StateFlow` and `MutableStateFlow`
- ViewModel unit tests with coroutine testing

**State Management Patterns:**
- Unidirectional data flow
- Immutable state objects
- Proper loading/error/success states
- Optimistic UI updates where appropriate

**Acceptance Criteria:**
- ViewModels survive configuration changes
- State updates are properly observed by UI
- No memory leaks in ViewModel lifecycle
- Comprehensive unit test coverage

---

#### **Task 4.2: Jetpack Compose UI**
**Assignee:** Lead Developer  
**Duration:** 5 days  
**Goal:** Build modern, intuitive user interface

**Deliverables:**
- `AlarmListScreen` - shows upcoming alarms with status
- `SettingsScreen` - app configuration and permissions
- `EventEditScreen` - for adding/editing alarm configurations
- `SyncStatusCard` - shows last sync time and manual sync button
- Material 3 design system implementation
- Proper accessibility support

**UI Requirements:**
- Clean, minimal design focused on functionality
- Clear visual indicators for alarm status
- Easy manual sync trigger
- Intuitive alarm configuration editing
- Responsive design for different screen sizes

**Acceptance Criteria:**
- UI follows Material Design 3 guidelines
- All interactive elements are accessible
- Smooth animations and transitions
- Works well on phones and tablets
- No UI performance issues or jank

---

#### **Task 4.3: Navigation & App Structure**
**Assignee:** Lead Developer  
**Duration:** 2 days  
**Goal:** Implement app navigation and overall structure

**Deliverables:**
- Navigation graph with Compose Navigation
- Bottom navigation or drawer navigation (based on UX decision)
- Deep linking support for alarm management
- Proper back stack management
- Integration with Android's back gesture

**Navigation Structure:**
- Home/Alarm List (default screen)
- Settings/Configuration
- Event Edit (modal or separate screen)
- About/Help (optional)

**Acceptance Criteria:**
- Navigation is intuitive and follows Android patterns
- Deep links work correctly
- Back navigation behaves as expected
- No navigation-related crashes

---

### **Sprint 5: Integration & Testing (Week 5-6)**

#### **Task 5.1: Dependency Injection with Hilt**
**Assignee:** Lead Developer  
**Duration:** 2 days  
**Goal:** Set up comprehensive dependency injection

**Deliverables:**
- `@HiltAndroidApp` application class
- Hilt modules for all major components
- Proper scoping for singletons and ViewModels
- Test doubles for unit testing
- Documentation for DI architecture

**DI Structure:**
- `DatabaseModule` - Room database and DAOs
- `RepositoryModule` - Repository implementations
- `ServiceModule` - Calendar and alarm services
- `WorkerModule` - WorkManager integration

**Acceptance Criteria:**
- All dependencies are properly injected
- No manual dependency creation in production code
- Test modules work correctly for unit tests
- DI graph validates successfully

---

#### **Task 5.2: Comprehensive Testing**
**Assignee:** Lead Developer  
**Duration:** 4 days  
**Goal:** Ensure app reliability through comprehensive testing

**Deliverables:**
- Unit tests for all business logic (90%+ coverage)
- Integration tests for database operations
- UI tests for critical user flows
- Manual testing checklist
- Performance testing results

**Testing Priorities:**
1. Alarm configuration parsing (critical)
2. Alarm scheduling and cancellation (critical)
3. Background sync reliability (high)
4. Permission handling (high)
5. UI state management (medium)

**Acceptance Criteria:**
- All unit tests pass consistently
- Integration tests cover happy path and error scenarios
- UI tests cover critical user journeys
- No memory leaks detected
- Performance meets acceptable thresholds

---

#### **Task 5.3: Edge Case Handling & Polish**
**Assignee:** Lead Developer  
**Duration:** 3 days  
**Goal:** Handle edge cases and polish user experience

**Deliverables:**
- Comprehensive error handling throughout app
- User-friendly error messages
- Offline capability where possible
- App state persistence across restarts
- Performance optimizations

**Edge Cases to Address:**
- Calendar events deleted after alarms scheduled
- System time changes (DST, timezone changes)
- App killed by system during sync
- Storage space limitations
- Network connectivity issues

**Acceptance Criteria:**
- App handles all identified edge cases gracefully
- No crashes in normal usage scenarios
- User experience remains smooth under stress
- Error messages are helpful and actionable

---

### **Sprint 6: Final Integration & Release Preparation (Week 6-8)**

#### **Task 6.1: End-to-End Testing**
**Assignee:** Lead Developer  
**Duration:** 3 days  
**Goal:** Validate complete user workflows

**Testing Scenarios:**
1. Fresh install → permission setup → calendar sync → alarm scheduling
2. Event modification → alarm rescheduling → notification delivery
3. App backgrounded → sync continues → alarms still trigger
4. Permission revoked → graceful degradation → re-enable flow
5. Multiple calendar accounts → proper event filtering → alarm management

**Acceptance Criteria:**
- All critical user journeys work end-to-end
- Performance is acceptable on target devices
- Battery usage is reasonable
- No data loss scenarios

---

#### **Task 6.2: Documentation & Deployment**
**Assignee:** Lead Developer  
**Duration:** 2 days  
**Goal:** Prepare for release and team handoff

**Deliverables:**
- Technical documentation for maintenance
- User guide for app functionality
- Release notes and changelog
- Play Store listing materials
- Monitoring and analytics setup

**Documentation Requirements:**
- Architecture overview with diagrams
- API documentation for major components
- Troubleshooting guide for common issues
- Performance benchmarks and targets

---

## 🎯 Success Metrics

### **Technical Metrics**
- **Alarm Accuracy:** 95%+ of alarms trigger within 2 seconds of target time
- **Sync Reliability:** 90%+ of background syncs complete successfully
- **Battery Impact:** <2% of daily battery usage
- **Crash Rate:** <0.1% of sessions
- **App Size:** <50MB installed

### **User Experience Metrics**
- **Permission Grant Rate:** >80% of users grant calendar permission
- **Feature Adoption:** >60% of users configure at least one alarm
- **Retention:** >70% of users still active after 1 week

---

## ⚠️ Risk Mitigation

### **High-Risk Areas**
1. **Background Processing Reliability**
   - **Risk:** Android battery optimization kills background sync
   - **Mitigation:** User education, fallback strategies, manual sync option

2. **Calendar Permission Denial**
   - **Risk:** Users deny calendar access, app becomes useless
   - **Mitigation:** Clear permission rationale, graceful degradation, easy re-enable

3. **Alarm Scheduling Precision**
   - **Risk:** Alarms don't trigger at exact times
   - **Mitigation:** Thorough testing on multiple devices, fallback notification system

4. **Platform Fragmentation**
   - **Risk:** Different behavior across Android versions/OEMs
   - **Mitigation:** Extensive device testing, adaptive behavior based on capabilities

### **Contingency Plans**
- **Scope Reduction:** Remove advanced features if timeline is tight
- **Platform Fallbacks:** Implement less-precise alternatives for restricted devices
- **Manual Overrides:** Always provide manual sync and alarm management options

---

## 📱 Device Testing Matrix

### **Priority Devices**
- **Google Pixel 6/7/8** (Stock Android)
- **Samsung Galaxy S22/S23** (One UI)
- **OnePlus 9/10** (OxygenOS)
- **Xiaomi Mi 11/12** (MIUI)

### **Android Version Coverage**
- **Android 13-14** (Primary target)
- **Android 11-12** (Secondary support)
- **Android 10** (Minimum support)

---

## 🔧 Development Environment Setup

### **Required Tools (2026)**
- Android Studio Koala | Ladybug or newer (2024.2+)
- JDK 21
- Android SDK 35 (compile SDK)
- Android Gradle Plugin 8.7+
- Git with conventional commit messages

### **Development Conventions**
- **Code Style:** Official Kotlin coding conventions + self-documenting code (per project rule)
- **Architecture:** Strict adherence to the PRD reconciliation rules
- **Git Flow:** Feature branches (`feature/*`), PRs with linked PRD acceptance criteria
- **Testing:** Minimum 85% coverage for business logic and parser
- **Documentation:** Comprehensive KDoc + architecture decision records

---

---

This plan provides clear ownership, deliverables, and success criteria for each phase of development. Each task includes specific technical requirements and acceptance criteria that can be used for code review and QA validation.