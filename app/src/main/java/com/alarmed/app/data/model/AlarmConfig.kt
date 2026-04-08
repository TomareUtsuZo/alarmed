package com.alarmed.app.data.model

/**
 * Represents a parsed alarm configuration block from a calendar event description.
 *
 * This data class holds the parsed values from the @alarmapp:v1 format
 * specified in the PRD.
 *
 * Example format in calendar event description:
 * ```
 * @alarmapp:v1
 * @enabled:true
 * @base:START
 * @alarms:90,45,15
 * @label:Meeting alarm
 * ```
 */
data class AlarmConfig(
    /** Whether this alarm configuration is enabled */
    val enabled: Boolean = true,

    /** Base time for offset calculations (currently only START is supported in MVP) */
    val base: AlarmBase = AlarmBase.START,

    /** List of minute offsets before the base time when alarms should trigger */
    val alarms: List<Int> = emptyList(),

    /** Optional label for the alarm (shown in notification) */
    val label: String? = null,

    /** Original raw text from the calendar event description */
    val rawText: String = ""
)

/** Base time reference for alarm offsets */
enum class AlarmBase {
    START,      // Event start time (MVP only)
    END         // Event end time (future extension)
}

/**
 * Result of parsing an alarm configuration block.
 * Used to distinguish between successful parsing, invalid blocks, and no block present.
 */
sealed class AlarmConfigParseResult {
    data class Success(val config: AlarmConfig) : AlarmConfigParseResult()
    data class Invalid(val reason: String) : AlarmConfigParseResult()
    object NotPresent : AlarmConfigParseResult()
}
