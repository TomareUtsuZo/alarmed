package com.alarmed.app.data.repository

import com.alarmed.app.data.dao.AlarmDao
import com.alarmed.app.data.model.AlarmBase
import com.alarmed.app.data.model.AlarmConfig
import com.alarmed.app.data.model.AlarmConfigParseResult
import com.alarmed.app.data.model.AlarmStatus
import com.alarmed.app.data.model.ScheduledAlarm
import com.alarmed.app.work.AlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive unit tests for AlarmRepositoryImpl.
 *
 * Tests the core reconciliation logic that is central to the entire application.
 * This is the "brain" that connects calendar events → parsed alarm configs → scheduled alarms.
 */
class AlarmRepositoryImplTest {

    private lateinit var alarmDao: AlarmDao
    private lateinit var calendarRepository: CalendarRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var repository: AlarmRepositoryImpl

    @Before
    fun setup() {
        alarmDao = mockk(relaxed = true)
        calendarRepository = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)
        // Use spy to allow real method calls while stubbing specific behaviors
        repository = spyk(AlarmRepositoryImpl(alarmDao, calendarRepository, alarmScheduler))
    }

    @Test
    fun `syncWithCalendar should process events with valid alarm configs`() = runTest {
        // Given
        val event = createTestCalendarEvent()
        coEvery { calendarRepository.getEventsWithAlarms(any(), any()) } returns listOf(event)
        coEvery { repository.reconcileAlarms(any(), any()) } returns Result.success(Unit)
        coEvery { alarmScheduler.scheduleAlarm(any()) } returns Unit

        // When - call real implementation via spy
        val result = repository.syncWithCalendar()

        // Then
        assertTrue(result.isSuccess)
        val syncResult = result.getOrThrow()
        assertEquals(1, syncResult.eventsProcessed)
        assertTrue(syncResult.alarmsCreated > 0)

        // Verify scheduler was called for each alarm
        coVerify { alarmScheduler.scheduleAlarm(any()) }
    }

    @Test
    fun `syncWithCalendar should cancel alarms for disabled configs`() = runTest {
        // Given
        val disabledConfig = AlarmConfig(enabled = false, alarms = listOf(30))
        val event = createTestCalendarEvent(disabledConfig)
        coEvery { calendarRepository.getEventsWithAlarms(any(), any()) } returns listOf(event)
        coEvery { repository.cancelAlarmsForEvent(any()) } returns Result.success(Unit)

        // When - call real implementation via spy
        val result = repository.syncWithCalendar()

        // Then
        assertTrue(result.isSuccess)
        coVerify { repository.cancelAlarmsForEvent(event.id) }
    }

    @Test
    fun `reconcileAlarms should cancel obsolete alarms and create new ones`() = runTest {
        // Given
        val eventId = "event-123"
        val existingAlarm = ScheduledAlarm.create(eventId, 1000, 30)
        val desiredAlarms = listOf(
            ScheduledAlarm.create(eventId, 1000, 15),
            ScheduledAlarm.create(eventId, 1000, 45)
        )

        coEvery { alarmDao.getAlarmsForEvent(eventId) } returns listOf(existingAlarm)
        coEvery { alarmDao.insertAlarm(any()) } returns 1L
        coEvery { alarmDao.deleteAlarmById(any()) } returns Unit

        // When
        val result = repository.reconcileAlarms(eventId, desiredAlarms)

        // Then
        assertTrue(result.isSuccess)
        
        // Should delete the obsolete alarm (30 min one)
        coVerify { alarmDao.deleteAlarmById(any()) }
        
        // Should insert the two new alarms
        coVerify(exactly = 2) { alarmDao.insertAlarm(any()) }
    }

    @Test
    fun `getUpcomingAlarms should delegate to DAO`() = runTest {
        val expectedAlarms = listOf(ScheduledAlarm.create("1", 1000, 15))
        every { alarmDao.getUpcomingAlarms(1000) } returns kotlinx.coroutines.flow.flowOf(expectedAlarms)

        val result = repository.getUpcomingAlarms(1000)

        assertEquals(expectedAlarms, result)
    }

    private fun createTestCalendarEvent(config: AlarmConfig = AlarmConfig(alarms = listOf(30, 15))): CalendarEvent {
        return CalendarEvent(
            id = "event-123",
            title = "Test Event",
            startTime = 1000000,
            endTime = 1100000,
            description = "@alarmapp:v1\n@alarms:30,15",
            calendarId = "cal-1",
            alarmConfig = AlarmConfigParseResult.Success(config)
        )
    }
}
