package com.blogspot.fdbozzo.lectorfeedsrss

import android.app.Application
import timber.log.Timber

class GlobalHookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}