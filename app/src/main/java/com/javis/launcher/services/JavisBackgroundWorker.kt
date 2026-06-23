package com.javis.launcher.services

import android.content.Context
import androidx.work.*
import com.javis.launcher.JavisApplication
import java.util.concurrent.TimeUnit

class JavisBackgroundWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val app = JavisApplication.get(applicationContext)
            app.memoryManager.initialize()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<JavisBackgroundWorker>(1, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "javis_background_sync", ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}
