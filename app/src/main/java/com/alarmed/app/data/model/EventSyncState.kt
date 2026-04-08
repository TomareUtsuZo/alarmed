package com.alarmed.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks sync state for calendar events to enable idempotent operations
 * and efficient reconciliation as defined in the PRD.
 *
 * This helps determine when events have changed and need their alarms
 * to be updated or canceled.
 */
@Entity(tableName = "event_sync_states")
data class EventSyncState(
    @PrimaryKey
    val calendarEventId: String,

    /**
     * Last seen updated marker from the calendar provider.
     * Could be a timestamp, etag, or sync token depending on the provider.
     */
    val lastSeenUpdatedMarker: String? = null,

    /**
     * Hash of the computed schedule for this event.
     * Used to detect when the alarm configuration has changed.
     */
    val lastComputedScheduleHash: String? = null,

    /** When this event was last synchronized */
    val lastSyncEpochMs: Long = System.currentTimeMillis(),

    /** Number of times this event has been synced */
    val syncCount: Int = 0
)
