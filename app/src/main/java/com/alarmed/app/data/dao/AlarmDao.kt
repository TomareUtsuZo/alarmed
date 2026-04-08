package com.alarmed.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alarmed.app.data.model.AlarmStatus
import com.alarmed.app.data.model.EventSyncState
import com.alarmed.app.data.model.ScheduledAlarm
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for alarm-related database operations.
 *
 * Implements all CRUD operations needed for idempotent alarm scheduling
 * and reconciliation as defined in the PRD.
 */
@Dao
interface AlarmDao {

    // ScheduledAlarm operations

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: ScheduledAlarm): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<ScheduledAlarm>)

    @Update
    suspend fun updateAlarm(alarm: ScheduledAlarm)

    @Delete
    suspend fun deleteAlarm(alarm: ScheduledAlarm)

    @Query("DELETE FROM scheduled_alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("SELECT * FROM scheduled_alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): ScheduledAlarm?

    @Query("SELECT * FROM scheduled_alarms WHERE calendarEventId = :calendarEventId")
    suspend fun getAlarmsForEvent(calendarEventId: String): List<ScheduledAlarm>

    @Query("SELECT * FROM scheduled_alarms WHERE triggerEpochMs > :currentTime ORDER BY triggerEpochMs ASC")
    fun getUpcomingAlarms(currentTime: Long): Flow<List<ScheduledAlarm>>

    @Query("SELECT * FROM scheduled_alarms WHERE status = :status")
    suspend fun getAlarmsByStatus(status: AlarmStatus): List<ScheduledAlarm>

    @Query("DELETE FROM scheduled_alarms WHERE calendarEventId = :calendarEventId")
    suspend fun deleteAlarmsForEvent(calendarEventId: String)

    @Query("DELETE FROM scheduled_alarms WHERE triggerEpochMs < :currentTime")
    suspend fun deletePastAlarms(currentTime: Long)

    // EventSyncState operations

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncState(syncState: EventSyncState)

    @Update
    suspend fun updateSyncState(syncState: EventSyncState)

    @Query("SELECT * FROM event_sync_states WHERE calendarEventId = :calendarEventId")
    suspend fun getSyncState(calendarEventId: String): EventSyncState?

    @Query("DELETE FROM event_sync_states WHERE calendarEventId = :calendarEventId")
    suspend fun deleteSyncState(calendarEventId: String)

    // Complex queries for reconciliation

    @Transaction
    @Query("""
        SELECT sa.* FROM scheduled_alarms sa
        WHERE sa.calendarEventId = :calendarEventId 
        AND sa.scheduleHash NOT IN (:currentHashes)
    """)
    suspend fun getObsoleteAlarms(
        calendarEventId: String,
        currentHashes: List<String>
    ): List<ScheduledAlarm>

    @Query("SELECT scheduleHash FROM scheduled_alarms WHERE calendarEventId = :calendarEventId")
    suspend fun getExistingAlarmHashes(calendarEventId: String): List<String>

    // Statistics and maintenance

    @Query("SELECT COUNT(*) FROM scheduled_alarms WHERE status = :status")
    suspend fun countAlarmsByStatus(status: AlarmStatus): Int

    @Query("SELECT COUNT(*) FROM scheduled_alarms WHERE triggerEpochMs > :currentTime")
    suspend fun countUpcomingAlarms(currentTime: Long): Int

    @Query("DELETE FROM scheduled_alarms")
    suspend fun deleteAllAlarms()

    @Query("DELETE FROM event_sync_states")
    suspend fun deleteAllSyncStates()
}
