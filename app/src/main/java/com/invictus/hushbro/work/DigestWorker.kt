package com.invictus.hushbro.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.invictus.hushbro.notify.DigestManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class DigestWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val KEY_FORCE = "force_digest_run"
    }

    override suspend fun doWork(): Result = try {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            DigestEntryPoint::class.java
        )
        entryPoint.digestManager().sendDigest(force = inputData.getBoolean(KEY_FORCE, false))
        Result.success()
    } catch (error: Throwable) {
        Result.retry()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DigestEntryPoint {
        fun digestManager(): DigestManager
    }
}
