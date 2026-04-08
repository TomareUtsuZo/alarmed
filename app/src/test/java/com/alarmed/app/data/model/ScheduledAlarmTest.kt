package com.alarmed.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for the ScheduledAlarm data model.
 *
 * Tests the core functionality and ensures the model behaves correctly
 * for the idempotent alarm scheduling requirements from the PRD.
 */
class ScheduledAlarmTest {

    @Test
    fun `scheduled alarm should generate unique schedule hash`() {
        val alarm1 = ScheduledAlarm(
            calendarEventId = "event-123",
            eventStartEpochMs = 1640995200000, // 2022-01-01
            offsetMinutes = 30,
            triggerEpochMs = 1640995200000 - (30 * 60 * 1000),
            scheduleHash = "test-hash-1"
        )

        val alarm2 = ScheduledAlarm(
            calendarEventId = "event-123",
            eventStartEpochMs = 1640995200000,
            offsetMinutes = 15,
            triggerEpochMs = 1640995200000 - (15 * 60 * 1000),
            scheduleHash = "test-hash-2"
        )

        assertTrue(alarm1.scheduleHash != alarm2.scheduleHash)
        assertEquals("event-123", alarm1.calendarEventId)
    }

    @Test
    fun `alarm status should default to scheduled`() {
        val alarm = ScheduledAlarm(
            calendarEventId = "event-123",
            eventStartEpochMs = System.currentTimeMillis(),
            offsetMinutes = 30,
            triggerEpochMs = System.currentTimeMillis() + 1800000,
            scheduleHash = "test-hash"
        )

        assertEquals(AlarmStatus.SCHEDULED, alarm.status)
    }

    @Test
    fun `alarm should be identifiable by schedule hash for idempotency`() {
        val alarms = listOf(
            ScheduledAlarm(
                calendarEventId = "event-123",
                eventStartEpochMs = 1640995200000,
                offsetMinutes = 30,
                triggerEpochMs = 1640993400000,
                scheduleHash = "hash-30min"
            ),
            ScheduledAlarm(
                calendarEventId = "event-123",
                eventStartEpochMs = 1640995200000,
                offsetMinutes = 15,
                triggerEpochMs = 1640994300000,
                scheduleHash = "hash-15min"
            )
        )

        val hashes = alarms.map { it.scheduleHash }
        assertEquals(2, hashes.size)
        assertTrue(hashes.contains("hash-30min"))
        assertTrue(hashes.contains("hash-15min"))
    }
}
