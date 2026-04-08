package com.alarmed.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Represents a scheduled alarm that was derived from a calendar event.
 *
 * This entity stores the relationship between a calendar event and the
 * specific alarm that should trigger at a calculated time.
 *
 * @see com.alarmed.app.data.model.EventSyncState for sync tracking
 */
@Entity(
    tableName = "scheduled_alarms",
    indices = [
        Index(value = ["calendarEventId"]),
        Index(value = ["triggerEpochMs"]),
        Index(value = ["scheduleHash"], unique = true)
    ]
)
data class ScheduledAlarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** The calendar event ID this alarm is associated with */
    val calendarEventId: String,

    /** The original event start time in milliseconds since epoch */
    val eventStartEpochMs: Long,

    /** Minutes before the event when this alarm should trigger */
    val offsetMinutes: Int,

    /** Calculated trigger time in milliseconds since epoch */
    val triggerEpochMs: Long,

    /**
     * Hash combining event ID, offset, and trigger time for idempotency.
     * Used to detect duplicate alarms and handle reconciliation.
     */
    val scheduleHash: String,

    /** Current status of this alarm */
    val status: AlarmStatus = AlarmStatus.SCHEDULED,

    /** When this alarm record was created */
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

/** Status of a scheduled alarm */
enum class AlarmStatus {
    SCHEDULED,
    FIRED,
    CANCELED
}
