package com.baishakhee.alarammanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class NightlyUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Perform nightly update tasks here
        Log.d("NightlyUpdateWorker", "Nightly update triggered!")
        // Simulate some work by delaying for a few seconds
        delay(5000) // 5 seconds
        // Indicate success or failure of the task
        return Result.success()
    }
}