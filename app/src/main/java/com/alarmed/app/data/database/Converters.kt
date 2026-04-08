package com.alarmed.app.data.database

import androidx.room.TypeConverter
import com.alarmed.app.data.model.AlarmStatus
import java.time.Instant

/**
 * Type converters for Room database to handle complex types.
 *
 * Converts between Kotlin types and SQLite primitive types.
 */
class Converters {

    @TypeConverter
    fun fromAlarmStatus(status: AlarmStatus): String {
        return status.name
    }

    @TypeConverter
    fun toAlarmStatus(status: String): AlarmStatus {
        return AlarmStatus.valueOf(status)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}
