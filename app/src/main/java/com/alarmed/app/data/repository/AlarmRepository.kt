package com.alarmed.app.data.repository

import com.alarmed.app.data.model.ScheduledAlarm
import com.alarmed.app.data.model.AlarmConfigParseResult

/**
 * Repository interface for alarm-related operations.
 *
 * Acts as the single source of truth for alarm data, coordinating between
 * the local database and calendar provider as defined in the Development Plan.
 */
interface AlarmRepository {

    /**
     * Gets all upcoming alarms that should trigger after the given time.
     */
    suspend fun getUpcomingAlarms(afterTime: Long): List<ScheduledAlarm>

    /**
     * Schedules a new alarm or updates an existing one.
     * Ensures idempotency using the schedule hash.
     */
    suspend fun scheduleAlarm(alarm: ScheduledAlarm): Result<Unit>

    /**
     * Cancels an alarm by its ID.
     */
    suspend fun cancelAlarm(alarmId: Long): Result<Unit>

    /**
     * Cancels all alarms for a specific calendar event.
     */
    suspend fun cancelAlarmsForEvent(calendarEventId: String): Result<Unit>

    /**
     * Synchronizes alarms with calendar events.
     * This is the main reconciliation method that implements the PRD algorithm.
     */
    suspend fun syncWithCalendar(): Result<SyncResult>

    /**
     * Reconciles desired alarms with existing alarms for an event.
     * Creates missing alarms and cancels obsolete ones.
     */
    suspend fun reconcileAlarms(
        calendarEventId: String,
        desiredAlarms: List<ScheduledAlarm>
    ): Result<Unit>
}

/**
 * Result of a sync operation with summary information.
 */
data class SyncResult(
    val eventsProcessed: Int,
    val alarmsCreated: Int,
    val alarmsCanceled: Int,
    val errors: List<String> = emptyList()
)
