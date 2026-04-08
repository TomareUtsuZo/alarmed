package com.alarmed.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarmed.app.data.database.AlarmDatabase
import com.alarmed.app.data.model.AlarmStatus
import com.alarmed.app.data.model.ScheduledAlarm
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Integration test for the Room database.
 *
 * Tests that the database can be created and basic operations work
 * as specified in the Development Plan acceptance criteria.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var database: AlarmDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AlarmDatabase::class.java
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `database should create successfully and support basic operations`() = runBlocking {
        val dao = database.alarmDao()

        // Create a test alarm
        val testAlarm = ScheduledAlarm(
            calendarEventId = "test-event-123",
            eventStartEpochMs = System.currentTimeMillis() + 3600000, // 1 hour from now
            offsetMinutes = 30,
            triggerEpochMs = System.currentTimeMillis() + 1800000, // 30 minutes from now
            scheduleHash = "test-hash-123"
        )

        // Insert the alarm
        val id = dao.insertAlarm(testAlarm)
        assertTrue(id > 0)

        // Query it back
        val alarms = dao.getAlarmsForEvent("test-event-123")
        assertEquals(1, alarms.size)
        assertEquals("test-event-123", alarms[0].calendarEventId)
        assertEquals(30, alarms[0].offsetMinutes)
    }

    @Test
    fun `database should support multiple alarms for same event`() = runBlocking {
        val dao = database.alarmDao()

        val alarms = listOf(
            ScheduledAlarm(
                calendarEventId = "multi-event",
                eventStartEpochMs = 1640995200000,
                offsetMinutes = 30,
                triggerEpochMs = 1640993400000,
                scheduleHash = "hash-30"
            ),
            ScheduledAlarm(
                calendarEventId = "multi-event",
                eventStartEpochMs = 1640995200000,
                offsetMinutes = 15,
                triggerEpochMs = 1640994300000,
                scheduleHash = "hash-15"
            )
        )

        dao.insertAlarms(alarms)

        val retrieved = dao.getAlarmsForEvent("multi-event")
        assertEquals(2, retrieved.size)
    }
}
