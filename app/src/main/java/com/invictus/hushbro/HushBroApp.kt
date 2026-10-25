package com.invictus.hushbro

import android.app.Application
import com.invictus.hushbro.notify.Channels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HushBroApp : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
            android.util.Log.i("HushBroApp", "sqlcipher loaded")
        } catch (e: Throwable) {
            android.util.Log.e("HushBroApp", "sqlcipher load failed", e)
        }
        try {
            Channels.createNotificationChannels(this)
        } catch (e: Throwable) {
            android.util.Log.e("HushBroApp", "Channel creation failed", e)
        }
    }
}
