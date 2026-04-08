**MVP** ** ** **PRD** ** ** **+** ** ** **Developer** ** ** **Checklist** ** ** **—** ** ** **Calendar-Driven** ** ** **Alarm** ** ** **App** ** ** **(Calendar** ** ** **→** ** ** **Device** ** ** **Alarms)**  
Product Strategy Team  
2026-04-03  
**Version:** 0.1 (MVP Spec)  
 **Date:** April 3, 2026  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAIAAAAECAYAAACk7+45AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAJUlEQVR4nAXBQQ0AIAwEsI7pmX8dJLjghYKjbQw0Nt7Cwa0k4ANVCQa1eRlSeQAAAABJRU5ErkJggg==)  
# [**SECTION A — MVP PRD**]()  
## [**1. Summary**]()  
Build a mobile app that uses the Android Calendar Provider to scan upcoming events (next 24 hours), detects events containing a structured alarm configuration block in the event Description, and schedules attention-grabbing device alarms at specified offsets before the event. The app also provides a UI to insert/update the alarm configuration block in calendar events. This MVP focuses on reliable, idempotent alarm scheduling using a polling-based sync mechanism.  
## [**2. Goals (MVP)**]()  
- Schedule device alarms that reliably capture the user's attention for configured calendar events  
- Idempotent behavior: no duplicate alarms; alarms are updated/canceled when the event or its alarm config changes  
- Simple, human-editable alarm config format embedded in the event description (minutes-before offsets only)  
- Polling-based sync on a recurring cadence (about every 15 minutes) to avoid needing backend infrastructure in MVP  
## [**3. Non-goals (MVP)**]()  
- Real-time push/webhook sync (defer)  
- Cross-attendee propagation of alarm settings for shared/invited events  
- Natural-language parsing of alarm instructions from free-form text  
- Advanced alarm polish (custom sounds, snooze UX, lock-screen visuals) beyond basic attention-grabbing alarms  
## [**4. Primary User Stories (MVP)**]()
1. **US1:** Grant calendar permissions so the app can read upcoming events and update event descriptions  
2. **US2:** Create/edit an event (via app or external calendar app) so the event description contains a valid alarm config block  
3. **US3:** When an eligible event is within the next 24 hours, schedule alarms at configured offsets  
4. **US4:** If event start time or alarm config changes, update/cancel/reschedule alarms without duplicates
## [**5. Alarm Configuration Block (MVP Format)**]()  
The alarm configuration is stored in the event Description/notes field using this exact format:  
@alarmapp:v1  
 @enabled:true  
 @base:START  
 @alarms:90,45,15  
 @label:Meeting alarm  
**MVP Rules:**  
- **Minutes-before only:** @alarms is a comma-separated list of positive integers, each representing minutes before @base  
- **@base is START only** in MVP (meaning event start time)  
- **@enabled:false** disables scheduling for that event without removing the block  
- **Unknown fields are ignored** (forward-compatible)  
## [**6. Functional Rules (MVP) — Rules Table**]()  
| | |  
|-|-|  
| Area | Rule |   
| **Sync cadence** | Run background sync on a recurring cadence (≈ every 15 minutes) |   
| **Time window** | On each sync, consider events whose start time is between now and now+24h |   
| **Eligibility** | Event is eligible only if Description contains @alarmapp:v1 and parses successfully |   
| **Enable/disable** | If @enabled:false then schedule no alarms for that event |   
| **Offsets** | Each @alarms entry is a positive integer minutes-before base time; duplicates removed |   
| **Alarm computation** | For each offset m: triggerTime = eventStartTime − m minutes. Skip triggers already in the past |   
| **Idempotency** | Do not create duplicates. Use a local registry to compare desired vs existing alarms |   
| **Reconciliation** | Create missing alarms; cancel obsolete alarms; update alarms when event/config changes |   
| **UI event setting** | App UI can insert/update the config block in an event Description with minutes-before offsets |   
| **Platform constraints** | If the platform restricts precise alarms/background execution, the app must degrade gracefully (e.g., less-precise alerts) |   
## [**7. Edge Cases (MVP)**]()  
- **Event start time edited** after alarms were scheduled → old alarms canceled; new alarms scheduled by next sync  
- **Alarm block edited** (offsets changed, enabled toggled) → reconcile accordingly by next sync  
- **Alarm block removed** or event deleted/canceled → alarms canceled by next sync  
- **Some computed alarm triggers are already in the past** at sync time → skip those triggers only  
- **Duplicate offsets** (e.g., 45,45,15) → treat as one  
- **Malformed config** (missing header, missing @alarms, non-integers) → treat as ineligible; do not schedule  
- **All-day events** without a concrete start time → ineligible in MVP  
- **Recurring events** → treat each instance with a concrete start time as schedulable  
- **Timezone/DST transitions** → compute based on the event's actual start moment in time  
## [**8. Acceptance Criteria (MVP)**]()  
- **AC1:** User can connect calendar and app can fetch events in the next 24 hours  
- **AC2:** Eligible events detected via @alarmapp:v1 and parsed into offsets; invalid blocks ignored  
- **AC3:** @enabled:false results in zero scheduled alarms for that event  
- **AC4:** For each eligible event and offset, an alarm is scheduled at eventStart - offset minutes if trigger time is in the future  
- **AC5:** Running sync twice without changes produces no duplicate alarms (idempotent)  
- **AC6:** If event start time changes, alarms are rescheduled (old canceled, new created) by the next sync  
- **AC7:** If block removed or event deleted, associated alarms are canceled by the next sync  
- **AC8:** If platform disallows precise alarms/background execution, app does not crash and follows fallback behavior  
- **AC9:** App UI can create/update event description to include a valid alarm config block with minutes-before offsets  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAIAAAAECAYAAACk7+45AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAI0lEQVR4nAXBwQkAIAwEsNT+uv+ELiGCcCaNgcbGXTh4lQR8VVwG3zSl62MAAAAASUVORK5CYII=)  
# [**SECTION B — Developer Checklist (MVP)**]()  
## [**A. Modules / Components**]()
- **Calendar Client:** read events via `CalendarContract` (now→now+24h), update event Description  
- **Block Parser:** detect `@alarmapp:v1` header, parse fields, validate offsets, ignore unknown keys  
- **Scheduler:** compute desired triggers from event start and offsets  
- **Alarm Dispatcher:** schedule/cancel alarms using AlarmManager + fallback behavior  
- **Alarm Registry (Room):** store scheduled alarms for idempotency and reconciliation  
- **Sync Orchestrator:** background job (`WorkManager`) that runs periodically and calls fetch→parse→reconcile  
- **Event Builder UI:** UI to help insert/update alarm config block in event descriptions
## [**B. Local Data Model (Minimum)**]()  
**1) ScheduledAlarm**  
- id (local primary key)  
- calendarEventId (string)  
- eventStartEpochMs (number)  
- offsetMinutes (int)  
- triggerEpochMs (number)  
- scheduleHash (string)  
- status (scheduled|fired|canceled) [optional for MVP]  
- createdAtEpochMs (number)  
**2) EventSyncState** (optional but helpful)  
- calendarEventId (string)  
- lastSeenUpdatedMarker (timestamp or etag if available)  
- lastComputedScheduleHash (string)  
- lastSyncEpochMs (number)  
## [**C. Reconciliation Steps (Every Sync Run)**]()  
1. Fetch events in [now, now+24h]  
2. For each event: detect @alarmapp:v1 in Description; if absent → skip  
3. Parse block; if invalid → skip; if @enabled:false → desired set empty  
4. Normalize offsets: parse integers, remove duplicates, discard non-positive  
5. Compute desired triggers: trigger = eventStart - offsetMinutes; discard triggers in the past  
6. Load existing ScheduledAlarm rows for that calendarEventId  
7. Compute ToCreate = desired - existing; ToCancel = existing - desired (compare by eventId+offset+trigger)  
8. Schedule alarms in ToCreate; cancel alarms in ToCancel via Alarm Dispatcher  
9. Persist registry updates: insert created alarms; mark canceled alarms; update sync state markers  
10. Log counts (created/canceled/skipped) and parse errors for debugging  
## [**D. Testing Checklist (MVP)**]()  
- **Happy path:** eligible event schedules N alarms at expected trigger times  
- **Idempotency:** run sync twice → no duplicates  
- **Edit start time:** reschedules correctly  
- **Edit offsets:** cancels obsolete, creates new  
- **Disable via @enabled:false:** cancels or avoids scheduling  
- **Malformed block:** ignored safely  
- **Past trigger:** skipped only for that trigger  
- **All-day event:** ignored in MVP  
- **Timezone/DST sanity checks**  
- **Platform restriction path:** fallback behavior exercised  
![](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAIAAAAECAYAAACk7+45AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAI0lEQVR4nDXBUQEAEBAFsBHgLgHd1FXq+bKNJL6LM7FQsNEPgiYEhNFQzYIAAAAASUVORK5CYII=)  
**💡 Implementation Note:** This MVP specification prioritizes reliability and simplicity over advanced features. The polling-based approach ensures consistent behavior across different mobile platforms while avoiding the complexity of real-time synchronization infrastructure.  
**⚠️ Platform Considerations:** Different mobile platforms have varying restrictions on background processing and precise alarm scheduling. The implementation must gracefully handle these constraints and provide appropriate fallback mechanisms to ensure user experience remains consistent.  
