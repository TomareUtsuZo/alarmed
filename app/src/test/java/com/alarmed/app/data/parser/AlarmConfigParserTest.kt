package com.alarmed.app.data.parser

import com.alarmed.app.data.model.AlarmBase
import com.alarmed.app.data.model.AlarmConfigParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive tests for the AlarmConfigParser.
 *
 * These tests validate that the parser correctly handles the exact format
 * specified in the PRD, including edge cases and error conditions.
 */
class AlarmConfigParserTest {

    private val parser = AlarmConfigParser()

    @Test
    fun `should parse valid alarm configuration block`() {
        val text = """
            Team sync meeting
            @alarmapp:v1
            @enabled:true
            @base:START
            @alarms:30,15,5
            @label:Prep for sync
        """.trimIndent()

        val result = parser.parse(text)

        assertTrue(result is AlarmConfigParseResult.Success)
        val success = result as AlarmConfigParseResult.Success
        val config = success.config

        assertTrue(config.enabled)
        assertEquals(AlarmBase.START, config.base)
        assertEquals(listOf(5, 15, 30), config.alarms) // sorted
        assertEquals("Prep for sync", config.label)
        assertTrue(config.rawText.contains("@alarmapp:v1"))
    }

    @Test
    fun `should handle not present case gracefully`() {
        val text = "Just a normal calendar event with no alarm block"
        val result = parser.parse(text)

        assertTrue(result is AlarmConfigParseResult.NotPresent)
    }

    @Test
    fun `should return invalid for missing alarms list`() {
        val text = """
            @alarmapp:v1
            @enabled:true
            @base:START
            @label:No alarms here
        """.trimIndent()

        val result = parser.parse(text)

        assertTrue(result is AlarmConfigParseResult.Invalid)
        val invalid = result as AlarmConfigParseResult.Invalid
        assertTrue(invalid.reason.contains("No valid alarm offsets"))
    }

    @Test
    fun `should parse disabled configuration`() {
        val text = """
            @alarmapp:v1
            @enabled:false
            @base:START
            @alarms:60
        """.trimIndent()

        val result = parser.parse(text)
        assertTrue(result is AlarmConfigParseResult.Success)
        
        val success = result as AlarmConfigParseResult.Success
        assertFalse(success.config.enabled)
    }

    @Test
    fun `should default to START base when not specified`() {
        val text = """
            @alarmapp:v1
            @alarms:45,15
        """.trimIndent()

        val result = parser.parse(text)
        assertTrue(result is AlarmConfigParseResult.Success)
        
        val success = result as AlarmConfigParseResult.Success
        assertEquals(AlarmBase.START, success.config.base)
    }

    @Test
    fun `containsAlarmBlock should return true for valid blocks`() {
        val text = "@alarmapp:v1\n@alarms:30"
        assertTrue(parser.containsAlarmBlock(text))
    }

    @Test
    fun `containsAlarmBlock should return false when no block present`() {
        val text = "No alarm configuration here"
        assertFalse(parser.containsAlarmBlock(text))
    }

    @Test
    fun `should handle malformed input gracefully`() {
        val text = "@alarmapp:v1\n@alarms:not_a_number"
        val result = parser.parse(text)
        
        // Should either succeed with empty alarms (filtered) or return Invalid
        when (result) {
            is AlarmConfigParseResult.Success -> assertTrue(result.config.alarms.isEmpty())
            is AlarmConfigParseResult.Invalid -> assertTrue(true) // expected
            else -> assertTrue("Should not be NotPresent", false)
        }
    }
}
