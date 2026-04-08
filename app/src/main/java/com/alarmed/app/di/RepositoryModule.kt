package com.alarmed.app.di

import android.content.Context
import com.alarmed.app.data.dao.AlarmDao
import com.alarmed.app.data.repository.AlarmRepository
import com.alarmed.app.data.repository.AlarmRepositoryImpl
import com.alarmed.app.data.repository.CalendarRepository
import com.alarmed.app.data.repository.CalendarRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 *
 * This follows the clean architecture pattern where repositories
 * act as the single source of truth for data operations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides the AlarmRepository implementation.
     */
    @Provides
    @Singleton
    fun provideAlarmRepository(
        alarmDao: AlarmDao
    ): AlarmRepository {
        return AlarmRepositoryImpl(alarmDao)
    }

    /**
     * Provides the CalendarRepository implementation.
     *
     * This will handle all interactions with the Android Calendar Provider.
     */
    @Provides
    @Singleton
    fun provideCalendarRepository(
        @ApplicationContext context: Context
    ): CalendarRepository {
        return CalendarRepositoryImpl(context)
    }
}
