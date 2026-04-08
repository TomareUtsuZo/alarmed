# Calendar-Driven Alarm App

An Android app that scans your calendar events for special alarm configuration blocks and automatically schedules precise device alarms.

## 📋 Overview

This app solves the problem of manually setting alarms for calendar events by embedding structured alarm instructions directly in event descriptions.

**Key Features:**
- Detects `@alarmapp:v1` configuration blocks in calendar event descriptions
- Automatically schedules multiple alarms at specified offsets before events
- Idempotent sync (no duplicate alarms)
- Graceful handling of permission changes and platform restrictions

## 📚 Documentation

- **[MVP PRD + Developer Checklist](documentation_development/MVP%20PRD%20+%20Developer%20Checklist%20%E2%80%94%20Calendar-Driven%20Alarm%20App%20(Calendar%20%E2%86%92%20Device%20Alarms).md)** — Detailed product requirements and implementation checklist
- **[Development Plan](documentation_development/Calendar-Driven%20Alarm%20App%20-%20Development%20Plan.md)** — Architecture, sprint breakdown, and execution plan

## 🏗️ Architecture

- **Calendar Access:** Android `CalendarContract` Provider (MVP decision)
- **Tech Stack:** Kotlin, Jetpack Compose, Room, WorkManager, Hilt, Coroutines
- **Core Pattern:** Periodic sync → Parse alarm blocks → Reconcile alarms (idempotent)

See `documentation_development/Calendar-Driven Alarm App - Development Plan.md` for full architecture decision record.

## 🚀 Getting Started

### Environment Setup

1. **Run the SDK setup script** (first time only):
   ```powershell
   .\setup-android-sdk.ps1
   ```

2. **Build the project**:
   ```powershell
   .\gradlew.bat assembleDebug
   ```

3. **Open in Android Studio** (recommended):
   - Open the project folder in Android Studio
   - Let it sync the Gradle files
   - Run the app on an emulator or device

### Quick Start

```powershell
# First time setup
.\setup-android-sdk.ps1

# Build the project
.\gradlew.bat assembleDebug

# Run the app (after building)
.\gradlew.bat installDebug
```

See the [Development Plan](documentation_development/Calendar-Driven%20Alarm%20App%20-%20Development%20Plan.md) for detailed architecture and implementation guide.

## 📋 Current Status

- **Build Status:** ✅ Successfully building (`.\gradlew.bat assembleDebug` produces `app-debug.apk` ~9.3MB)
- **Version Compatibility:** ✅ Resolved (Kotlin 1.9.24 + Compose 1.6.8 + Compiler 1.5.14 aligned per plan)
- **Tests:** Unit tests need test dependencies added (in progress)
- **Phase:** Foundation complete → Moving into Sprint 1 implementation
- **Architecture:** Android Calendar Provider (CalendarContract) confirmed for MVP

---

**Note:** This project follows self-documenting code principles and strict adherence to the PRD reconciliation rules. The `Version-Compatibility-Plan.md` served as our single source of truth to resolve dependency conflicts.
