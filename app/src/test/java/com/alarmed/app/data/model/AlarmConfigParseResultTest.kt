package com.alarmed.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the alarm configuration parsing result types.
 *
 * These tests ensure the parsing result sealed class works correctly
 * for the different scenarios defined in the PRD (valid, invalid, not present).
 */
class AlarmConfigParseResultTest {

    @Test
    fun `success result should contain valid alarm config`() {
        val config = AlarmConfig(
            enabled = true,
            alarms = listOf(30, 15, 5),
            label = "Team Meeting"
        )

        val result = AlarmConfigParseResult.Success(config)

        assertTrue(result is AlarmConfigParseResult.Success)
        val successResult = result as AlarmConfigParseResult.Success
        assertEquals(3, successResult.config.alarms.size)
        assertEquals("Team Meeting", successResult.config.label)
        assertTrue(successResult.config.enabled)
    }

    @Test
    fun `invalid result should contain reason`() {
        val result = AlarmConfigParseResult.Invalid("Missing @alarmapp:v1 header")

        assertTrue(result is AlarmConfigParseResult.Invalid)
        val invalidResult = result as AlarmConfigParseResult.Invalid
        assertEquals("Missing @alarmapp:v1 header", invalidResult.reason)
    }

    @Test
    fun `not present result should be singleton instance`() {
        val result1 = AlarmConfigParseResult.NotPresent
        val result2 = AlarmConfigParseResult.NotPresent

        assertTrue(result1 is AlarmConfigParseResult.NotPresent)
        assertTrue(result2 is AlarmConfigParseResult.NotPresent)

        // Should be the same instance (object)
        assertTrue(result1 === result2)
    }

    @Test
    fun `parse results should be distinguishable by type`() {
        val results = listOf(
            AlarmConfigParseResult.Success(AlarmConfig()),
            AlarmConfigParseResult.Invalid("Bad format"),
            AlarmConfigParseResult.NotPresent
        )

        val successCount = results.count { it is AlarmConfigParseResult.Success }
        val invalidCount = results.count { it is AlarmConfigParseResult.Invalid }
        val notPresentCount = results.count { it is AlarmConfigParseResult.NotPresent }

        assertEquals(1, successCount)
        assertEquals(1, invalidCount)
        assertEquals(1, notPresentCount)
    }
}
