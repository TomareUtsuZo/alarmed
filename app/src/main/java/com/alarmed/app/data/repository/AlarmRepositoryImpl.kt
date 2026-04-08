package com.alarmed.app.data.repository

import com.alarmed.app.data.dao.AlarmDao
import com.alarmed.app.data.model.AlarmBase
import com.alarmed.app.data.model.ScheduledAlarm
import com.alarmed.app.data.model.AlarmConfigParseResult
import com.alarmed.app.data.model.AlarmStatus
import com.alarmed.app.work.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

/**
 * Production implementation of AlarmRepository.
 *
 * This is the core reconciliation engine described in the Development Plan.
 * It implements the idempotent scheduling algorithm that ensures:
 * 
 * 1. No duplicate alarms are created for the same event+offset combination
 * 2. Alarms are only scheduled for events that have valid @alarmapp blocks
 * 3. Old alarms are cleaned up when configurations change
 * 4. Everything is stored in Room with proper indexing for performance
 */
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val calendarRepository: CalendarRepository,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {

    override suspend fun getUpcomingAlarms(afterTime: Long): List<ScheduledAlarm> = withContext(Dispatchers.IO) {
        alarmDao.getUpcomingAlarms(afterTime).first()
    }

    override suspend fun scheduleAlarm(alarm: ScheduledAlarm): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // The DAO handles idempotency via the scheduleHash primary key
            alarmDao.insertAlarm(alarm)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelAlarm(alarmId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            alarmDao.deleteAlarmById(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelAlarmsForEvent(calendarEventId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            alarmDao.deleteAlarmsForEvent(calendarEventId)
            // Also cancel any pending WorkManager work for these alarms
            alarmScheduler.cancelAlarmsForEvent(calendarEventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Main reconciliation method - the heart of the application.
     * 
     * This implements the algorithm described in the PRD and Development Plan:
     * 1. Fetch calendar events that have alarm blocks in the near future
     * 2. Parse the alarm configurations
     * 3. Calculate when alarms should trigger
     * 4. Reconcile with existing alarms in the database (idempotent)
     */
    override suspend fun syncWithCalendar(): Result<SyncResult> = withContext(Dispatchers.IO) {
        val now = Instant.now().toEpochMilli()
        val windowEnd = now + (7 * 24 * 60 * 60 * 1000) // Next 7 days

        val events = calendarRepository.getEventsWithAlarms(now, windowEnd)
        var alarmsCreated = 0
        var alarmsCanceled = 0
        val errors = mutableListOf<String>()

        for (event in events) {
            try {
                val parseResult = event.alarmConfig
                if (parseResult is AlarmConfigParseResult.Success) {
                    val config = parseResult.config
                    
                    if (!config.enabled) {
                        // Cancel any existing alarms for disabled configs
                        cancelAlarmsForEvent(event.id)
                        alarmsCanceled++
                        continue
                    }

                    val desiredAlarms = config.alarms.map { offsetMinutes ->
                        val triggerTime = when (config.base) {
                            AlarmBase.START -> event.startTime + (offsetMinutes * 60 * 1000L)
                            AlarmBase.END -> (event.endTime ?: event.startTime) + (offsetMinutes * 60 * 1000L)
                        }
                        
                        ScheduledAlarm.create(
                            calendarEventId = event.id,
                            eventStartEpochMs = event.startTime,
                            offsetMinutes = offsetMinutes,
                            label = config.label
                        )
                    }

                    val reconcileResult = reconcileAlarms(event.id, desiredAlarms)
                    if (reconcileResult.isSuccess) {
                        alarmsCreated += desiredAlarms.size
                    }
                }
            } catch (e: Exception) {
                errors.add("Failed to process event ${event.id}: ${e.message}")
            }
        }

        Result.success(
            SyncResult(
                eventsProcessed = events.size,
                alarmsCreated = alarmsCreated,
                alarmsCanceled = alarmsCanceled,
                errors = errors
            )
        )
    }

    override suspend fun reconcileAlarms(
        calendarEventId: String,
        desiredAlarms: List<ScheduledAlarm>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Cancel any existing alarms for this event that are no longer desired
            val existingAlarms = alarmDao.getAlarmsForEvent(calendarEventId)
            val desiredHashes = desiredAlarms.map { it.scheduleHash }.toSet()

            existingAlarms.forEach { existing ->
                if (!desiredHashes.contains(existing.scheduleHash)) {
                    alarmDao.deleteAlarmById(existing.id)
                }
            }

            // Schedule all desired alarms (DAO handles idempotency via unique constraint)
            desiredAlarms.forEach { alarm ->
                alarmDao.insertAlarm(alarm)
                alarmScheduler.scheduleAlarm(alarm)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
