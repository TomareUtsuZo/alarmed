package com.alarmed.app.data.repository

import com.alarmed.app.data.model.AlarmConfigParseResult

/**
 * Repository interface for calendar operations.
 *
 * Handles all interactions with the Android Calendar Provider (CalendarContract).
 * This abstraction allows for easier testing and future extension to other calendar sources.
 */
interface CalendarRepository {

    /**
     * Fetches calendar events that fall within the specified time window.
     * Only returns events that have valid alarm configuration blocks.
     *
     * @param startTime Start of the time window (milliseconds since epoch)
     * @param endTime End of the time window (milliseconds since epoch)
     * @return List of calendar events with their parsed alarm configurations
     */
    suspend fun getEventsWithAlarms(
        startTime: Long,
        endTime: Long
    ): List<CalendarEvent>

    /**
     * Updates a calendar event's description with an alarm configuration block.
     * Used by the UI to save user-configured alarm settings.
     */
    suspend fun updateEventAlarmConfig(
        eventId: String,
        config: String
    ): Result<Unit>

    /**
     * Checks if the app has the necessary calendar permissions.
     */
    suspend fun hasCalendarPermission(): Boolean

    /**
     * Requests calendar permissions from the user.
     */
    suspend fun requestCalendarPermission(): Boolean
}

/**
 * Represents a calendar event with its parsed alarm configuration.
 */
data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long?,
    val description: String,
    val calendarId: String,
    val alarmConfig: AlarmConfigParseResult
)
