package com.baishakhee.alarammanager

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService


class MyIntentService : JobIntentService() {

    companion object {
        private const val JOB_ID = 1000
    }

    override fun onHandleWork(intent: Intent) {
        // Handle the alarm logic here
        // You can also play a sound or display a notification here if needed
        // For example, you can call showNotification() here
        showNotification(applicationContext)
    }

    private fun showNotification(context: Context) {
        // Your notification code goes here
        // Example code is provided in the previous responses
        // ...
    }

    // Convenience method for enqueuing work in this service
    fun enqueueWork(context: Context, work: Intent) {
        enqueueWork(context, MyIntentService::class.java, JOB_ID, work)
    }
}