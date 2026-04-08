package com.alarmed.app.di

import android.content.Context
import androidx.room.Room
import com.alarmed.app.data.database.AlarmDatabase
import com.alarmed.app.data.dao.AlarmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 *
 * This module provides the Room database instance and DAOs as singletons
 * to ensure consistent database access throughout the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance as a singleton.
     *
     * The database is configured with the name specified in AlarmDatabase
     * and will be created in the app's data directory.
     *
     * Note: For production, we should implement proper migrations instead
     * of fallbackToDestructiveMigration as mentioned in the Development Plan.
     */
    @Provides
    @Singleton
    fun provideAlarmDatabase(
        @ApplicationContext context: Context
    ): AlarmDatabase {
        return Room.databaseBuilder(
            context,
            AlarmDatabase::class.java,
            AlarmDatabase.DATABASE_NAME
        )
            // For MVP development - this allows schema changes without migration
            // In production, we would implement proper migrations for each version
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the AlarmDao for database operations.
     *
     * Since the DAO is obtained from the database instance, it's also a singleton.
     */
    @Provides
    @Singleton
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao {
        return database.alarmDao()
    }
}
