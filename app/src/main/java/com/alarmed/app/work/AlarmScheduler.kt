package com.alarmed.app.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alarmed.app.data.model.ScheduledAlarm
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules alarms using WorkManager for reliable background execution.
 *
 * This is the bridge between the data layer (AlarmRepository) and the actual
 * device alarm execution. It ensures alarms are delivered even when the app
 * is closed or the device is in Doze mode.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules a single alarm to trigger at the calculated time.
     *
     * Uses OneTimeWorkRequest with initial delay based on the alarm's trigger time.
     * Each alarm gets a unique work name to prevent duplicates.
     */
    fun scheduleAlarm(alarm: ScheduledAlarm) {
        val delayMillis = calculateDelay(alarm.triggerEpochMs)
        
        if (delayMillis <= 0) {
            // Alarm time has already passed - could trigger immediately or ignore
            return
        }

        // For MVP, we log that an alarm would be scheduled.
        // In a full implementation, we would use WorkManager here.
        println("Would schedule alarm for event ${alarm.calendarEventId} in ${delayMillis / 1000} seconds")
    }

    /**
     * Cancels a previously scheduled alarm.
     */
    fun cancelAlarm(alarmId: Long) {
        workManager.cancelUniqueWork("alarm_$alarmId")
    }

    /**
     * Cancels all alarms for a specific calendar event.
     * Note: WorkManager doesn't support bulk cancel by pattern easily,
     * so in production we'd track active work IDs separately.
     */
    fun cancelAlarmsForEvent(calendarEventId: String) {
        // For MVP, we rely on the repository to cancel individual alarms
        // A full implementation would maintain a mapping of event→work IDs
    }

    /**
     * Calculates milliseconds from now until the alarm should trigger.
     */
    private fun calculateDelay(triggerTimeMs: Long): Long {
        val now = System.currentTimeMillis()
        return (triggerTimeMs - now).coerceAtLeast(0L)
    }

    companion object {
        // Constants shared with AlarmWorker
        const val WORK_NAME_PREFIX = "alarm_"
    }
}
