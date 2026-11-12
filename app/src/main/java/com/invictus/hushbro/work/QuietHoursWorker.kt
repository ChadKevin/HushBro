package com.invictus.hushbro.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.notify.QuietHoursManager
import com.invictus.hushbro.notify.QuietHoursScheduler
import com.invictus.hushbro.notify.QuietHoursTime
import kotlinx.coroutines.flow.first

/** Runs at each quiet-hours boundary and schedules the next boundary. */
class QuietHoursWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        val settings = SettingsStore(applicationContext).userSettingsFlow.first()
        val now = System.currentTimeMillis()
        QuietHoursManager.sync(applicationContext, settings, now)
        val nextTransition = QuietHoursTime.nextTransitionMillis(settings, now)
        if (nextTransition != null) {
            QuietHoursScheduler.enqueueNext(applicationContext, nextTransition - now)
        }
        Result.success()
    } catch (error: Throwable) {
        Log.e("HushBroQuietHours", "Could not refresh quiet-hours state", error)
        Result.retry()
    }
}
