package com.alarmed.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class.
 * 
 * Annotated with @HiltAndroidApp to trigger Hilt code generation
 * for dependency injection throughout the app.
 */
@HiltAndroidApp
class AlarmApp : Application()
