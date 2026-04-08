package com.alarmed.app.data.parser

import com.alarmed.app.data.model.AlarmBase
import com.alarmed.app.data.model.AlarmConfig
import com.alarmed.app.data.model.AlarmConfigParseResult

/**
 * Parses structured alarm configuration blocks from calendar event descriptions.
 *
 * This is the core parser for the @alarmapp:v1 format defined in the PRD.
 * It looks for blocks starting with `@alarmapp:v1` and extracts configuration
 * parameters like enabled state, base time, alarm offsets, and labels.
 *
 * Example format in a calendar event description:
 * ```
 * Meeting with team
 * @alarmapp:v1
 * @enabled:true
 * @base:START
 * @alarms:90,45,15
 * @label:Prep time
 * ```
 *
 * The parser is designed to be robust, idempotent, and provide clear error
 * messages for malformed configurations.
 */
class AlarmConfigParser {

    /**
     * Parses an alarm configuration block from event description text.
     *
     * @param text The full description text from a calendar event
     * @return [AlarmConfigParseResult] indicating success, invalid format, or no block present
     */
    fun parse(text: String): AlarmConfigParseResult {
        if (text.isBlank()) {
            return AlarmConfigParseResult.NotPresent
        }

        // Look for the alarmapp header - this marks the start of a config block
        val lines = text.lines()
        val headerIndex = lines.indexOfFirst { it.trim().startsWith("@alarmapp:v1") }

        if (headerIndex == -1) {
            return AlarmConfigParseResult.NotPresent
        }

        return try {
            parseConfigBlock(lines.drop(headerIndex))
        } catch (e: Exception) {
            AlarmConfigParseResult.Invalid("Failed to parse alarm block: ${e.message}")
        }
    }

    /**
     * Parses the actual configuration values from lines starting with the @alarmapp:v1 header.
     */
    private fun parseConfigBlock(lines: List<String>): AlarmConfigParseResult {
        var enabled = true
        var base = AlarmBase.START
        val alarms = mutableListOf<Int>()
        var label: String? = null
        val rawText = lines.joinToString("\n")

        for (line in lines) {
            val trimmed = line.trim()
            if (!trimmed.startsWith("@")) continue

            when {
                trimmed.startsWith("@enabled:") -> {
                    enabled = trimmed.substringAfter(":").trim().lowercase() == "true"
                }
                trimmed.startsWith("@base:") -> {
                    val baseValue = trimmed.substringAfter(":").trim().uppercase()
                    base = when (baseValue) {
                        "START" -> AlarmBase.START
                        "END" -> AlarmBase.END
                        else -> AlarmBase.START // default for MVP
                    }
                }
                trimmed.startsWith("@alarms:") -> {
                    val alarmValues = trimmed.substringAfter(":").trim()
                    alarms.addAll(
                        alarmValues.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .mapNotNull { it.toIntOrNull() }
                            .filter { it > 0 } // only positive offsets make sense
                    )
                }
                trimmed.startsWith("@label:") -> {
                    label = trimmed.substringAfter(":").trim()
                }
            }
        }

        // If we have no alarms defined, this is invalid per PRD
        if (alarms.isEmpty()) {
            return AlarmConfigParseResult.Invalid("No valid alarm offsets found in @alarms")
        }

        val config = AlarmConfig(
            enabled = enabled,
            base = base,
            alarms = alarms.sorted(), // ensure consistent ordering
            label = label?.takeIf { it.isNotBlank() },
            rawText = rawText
        )

        return AlarmConfigParseResult.Success(config)
    }

    /**
     * Convenience method to check if text contains any alarm configuration block.
     */
    fun containsAlarmBlock(text: String): Boolean {
        return parse(text) is AlarmConfigParseResult.Success
    }
}
