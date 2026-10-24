package com.invictus.hushbro.work

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.invictus.hushbro.notify.DigestManager
import dagger.hilt.android.EntryPointAccessors

class DigestWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            DigestEntryPoint::class.java
        )
        entryPoint.digestManager().sendDigest()
        return Result.success()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DigestEntryPoint {
        fun digestManager(): DigestManager
    }
}
