package com.alarmed.app.di

import com.alarmed.app.data.parser.AlarmConfigParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing parser components.
 *
 * This keeps parsing logic cleanly separated and injectable.
 * The AlarmConfigParser is stateless and can be a singleton.
 */
@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    /**
     * Provides the AlarmConfigParser as a singleton.
     *
     * This parser is the core business logic for interpreting
     * structured alarm blocks from calendar event descriptions.
     */
    @Provides
    @Singleton
    fun provideAlarmConfigParser(): AlarmConfigParser {
        return AlarmConfigParser()
    }
}
