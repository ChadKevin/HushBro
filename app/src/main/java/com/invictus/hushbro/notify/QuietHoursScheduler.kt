package com.invictus.hushbro.notify

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.invictus.hushbro.work.QuietHoursWorker
import java.util.concurrent.TimeUnit

/** Schedules only quiet-hours start/end transitions. WorkManager persists this across process death/reboot. */
object QuietHoursScheduler {
    const val UNIQUE_WORK_NAME = "hushbro_quiet_hours_transition"

    fun refresh(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        val request = OneTimeWorkRequestBuilder<QuietHoursWorker>()
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun enqueueNext(context: Context, delayMillis: Long) {
        val request = OneTimeWorkRequestBuilder<QuietHoursWorker>()
            .setInitialDelay(delayMillis.coerceAtLeast(1_000L), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}
