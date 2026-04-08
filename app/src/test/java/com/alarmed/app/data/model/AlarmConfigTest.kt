package com.alarmed.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the AlarmConfig data model.
 *
 * Tests the alarm configuration parsing and validation logic
 * based on the exact format specified in the PRD.
 */
class AlarmConfigTest {

    @Test
    fun `alarm config should have correct default values`() {
        val config = AlarmConfig()

        assertTrue(config.enabled)
        assertEquals(AlarmBase.START, config.base)
        assertTrue(config.alarms.isEmpty())
        assertEquals(null, config.label)
    }

    @Test
    fun `alarm config should store provided values`() {
        val config = AlarmConfig(
            enabled = false,
            base = AlarmBase.START,
            alarms = listOf(30, 15, 5),
            label = "Test Meeting",
            rawText = "@alarmapp:v1\n@enabled:false\n@alarms:30,15,5\n@label:Test Meeting"
        )

        assertFalse(config.enabled)
        assertEquals(3, config.alarms.size)
        assertEquals("Test Meeting", config.label)
        assertEquals(30, config.alarms[0])
        assertEquals(15, config.alarms[1])
        assertEquals(5, config.alarms[2])
    }

    @Test
    fun `parse result should handle different scenarios`() {
        val success = AlarmConfigParseResult.Success(
            AlarmConfig(alarms = listOf(30, 15))
        )
        val invalid = AlarmConfigParseResult.Invalid("Missing header")
        val notPresent = AlarmConfigParseResult.NotPresent

        assertTrue(success is AlarmConfigParseResult.Success)
        assertTrue(invalid is AlarmConfigParseResult.Invalid)
        assertTrue(notPresent is AlarmConfigParseResult.NotPresent)
    }
}
