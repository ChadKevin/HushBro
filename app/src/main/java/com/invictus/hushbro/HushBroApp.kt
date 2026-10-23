package com.invictus.hushbro

import android.app.Application
import com.invictus.hushbro.notify.Channels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HushBroApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Channels.createNotificationChannels(this)
    }
}
