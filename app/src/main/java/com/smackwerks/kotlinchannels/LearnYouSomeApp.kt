package com.smackwerks.kotlinchannels

import android.app.Application
import timber.log.Timber

class LearnYouSomeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}