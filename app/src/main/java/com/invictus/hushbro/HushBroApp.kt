package com.invictus.hushbro

import android.app.Application
import com.invictus.hushbro.notify.Channels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HushBroApp : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            // Load SQLCipher native library before any database operations.
            // Without this, all JNI native calls fail with UnsatisfiedLinkError
            // and the app crashes on first database access.
            System.loadLibrary("sqlcipher")
        } catch (e: Throwable) {
            // Library may already be loaded or unavailable; database will fall back to plain Room.
        }
        try {
            Channels.createNotificationChannels(this)
        } catch (e: Throwable) {
            // Notification channel creation must never prevent the app from starting.
        }
    }
}
