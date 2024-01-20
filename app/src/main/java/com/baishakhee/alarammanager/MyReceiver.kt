package com.baishakhee.alarammanager

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MyReceiver : BroadcastReceiver() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 123
        private const val PERMISSION_REQUEST_CODE = 123 // Use any unique code

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("MyReceiver", "onReceive called")
        val message = intent?.getStringExtra("notification_message")

        // Play sound
        playAlarmSound(context)
        Log.d("MyReceiver", "onReceive playAlarmSound")

        showNotification(context,message)
        Log.d("MyReceiver", "onReceive showNotification")

        Toast.makeText(context, "Alarm triggered!", Toast.LENGTH_SHORT).show()

    }

    private fun playAlarmSound(context: Context?) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showNotification(context: Context?,message:String?) {
        createNotificationChannel(context)
        Log.d("MyReceiver", "onReceive showNotification....")

        val notificationBuilder = buildNotification(context,message)

        with(NotificationManagerCompat.from(context!!)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.VIBRATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.VIBRATE),
                    PERMISSION_REQUEST_CODE
                )
                return
            }
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MyReceiver", "onReceive createNotificationChannel..0000000....")

            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm notifications"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000) // Vibrate pattern
            }

            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        Log.d("MyReceiver", "onReceive createNotificationChannel..00....")


    }

    private fun buildNotification(context: Context?,message:String?): NotificationCompat.Builder {
        Log.d("MyReceiver", "onReceive buildNotification..00....")

        return NotificationCompat.Builder(context!!, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .setContentTitle("Alarm Notification")
            .setContentText("This is your $message alarm notification.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(getDefaultNotificationSound())
    }

    private fun getDefaultNotificationSound(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }
}