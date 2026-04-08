package com.alarmed.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alarmed.app.data.dao.AlarmDao
import com.alarmed.app.data.model.EventSyncState
import com.alarmed.app.data.model.ScheduledAlarm

/**
 * Room database for the Calendar-Driven Alarm app.
 *
 * Contains entities for scheduled alarms and sync state tracking
 * to support the idempotent reconciliation algorithm defined in the PRD.
 */
@Database(
    entities = [
        ScheduledAlarm::class,
        EventSyncState::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "alarm_database"
    }
}
