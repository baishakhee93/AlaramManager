package com.baishakhee.alarammanager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val ALARM_INTERVAL = 24 * 60 * 60 * 1000 // 24 hours
    private  val PERMISSION_REQUEST_CODE = 123 // Use any unique code
    private val myReceiver = MyReceiver()
    private lateinit var timePicker: TimePicker
    private lateinit var btnSetAlarm: Button
    private lateinit var dateTextView: TextView
    private lateinit var message: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timePicker = findViewById(R.id.timePicker)
        btnSetAlarm = findViewById(R.id.btnSetAlarm)
        dateTextView = findViewById(R.id.dateTextView)
        message = findViewById(R.id.alarmMessage)
        checkNotificationPermission()

        dateTextView.setOnClickListener {
            showDatePickerDialog()
        }

        btnSetAlarm.setOnClickListener {
            scheduleAlarm()
        }
        // Schedule nightly update using WorkManager
        scheduleNightlyUpdate()

        // Schedule alarm
      //  scheduleAlarm()
    }
    private fun showDatePickerDialog() {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // DatePickerDialog
        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            // Update TextView with selected date
            val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
            findViewById<TextView>(R.id.dateTextView).text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }
    private fun scheduleAlarm() {
        val dateTime = Calendar.getInstance()
        val dateString = dateTextView.text.toString()
        val dateParts = dateString.split("/")

        val hour: Int = if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour
        } else {
            timePicker.currentHour
        }

        val minute: Int = if (Build.VERSION.SDK_INT >= 23) {
            timePicker.minute
        } else {
            timePicker.currentMinute
        }

        // Set the date and time for the alarm
        dateTime.set(Calendar.YEAR, dateParts[2].toInt())
        dateTime.set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-based
        dateTime.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
        dateTime.set(Calendar.HOUR_OF_DAY, hour)
        dateTime.set(Calendar.MINUTE, minute)
        dateTime.set(Calendar.SECOND, 0)

        // Get the message from EditText
        val messages = message.text.toString()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, MyReceiver::class.java).let { intent ->
            // Put the message in the Intent
            intent.putExtra("notification_message", messages)
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        if (System.currentTimeMillis() > dateTime.timeInMillis) {
            dateTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Check if the device supports scheduling exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (alarmManager.canScheduleExactAlarms()) {
                    // The device supports scheduling exact alarms
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        dateTime.timeInMillis,
                        alarmIntent
                    )
                    Toast.makeText(
                        this,
                        "Alarm Set Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // The device does not support scheduling exact alarms
                    // Handle this situation accordingly
                    Toast.makeText(
                        this,
                        "Device does not support scheduling exact alarms",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: SecurityException) {
                // Handle SecurityException if thrown
                e.printStackTrace()
                Toast.makeText(this, "SecurityException: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // For devices before Android 12, use setExact without checking
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                dateTime.timeInMillis,
                alarmIntent
            )
            Toast.makeText(
                this,
                "Alarm Set Successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /*@SuppressLint("UnspecifiedImmutableFlag")
    private fun scheduleAlarm() {
        val dateTime = Calendar.getInstance()
        val dateString = dateTextView.text.toString()
        val dateParts = dateString.split("/")

        val hour: Int = if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour
        } else {
            timePicker.currentHour
        }

        val minute: Int = if (Build.VERSION.SDK_INT >= 23) {
            timePicker.minute
        } else {
            timePicker.currentMinute
        }

        // Set the date and time for the alarm
        dateTime.set(Calendar.YEAR, dateParts[2].toInt())
        dateTime.set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-based
        dateTime.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
        dateTime.set(Calendar.HOUR_OF_DAY, hour)
        dateTime.set(Calendar.MINUTE, minute)
        dateTime.set(Calendar.SECOND, 0)
        val messages = message.text.toString()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, MyReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        if (System.currentTimeMillis() > dateTime.timeInMillis) {
            dateTime.add(Calendar.DAY_OF_YEAR, 1)
        }
       *//* val hour: Int
        val minute: Int
        if (Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.hour
            minute = timePicker.minute
        } else {
            hour = timePicker.currentHour
            minute = timePicker.currentMinute
        }
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, MyReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        // Set the alarm to start at 3:40 PM and repeat every 24 hours
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        // If the time is already past 3:40 PM, start the alarm tomorrow
        if (System.currentTimeMillis() > calendar.timeInMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
*//*
        // Check if the device supports scheduling exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (alarmManager.canScheduleExactAlarms()) {
                    // The device supports scheduling exact alarms
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        dateTime.timeInMillis,
                        alarmIntent
                    )
                    Toast.makeText(
                        this,
                        "Alaram Set Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // The device does not support scheduling exact alarms
                    // Handle this situation accordingly
                    Toast.makeText(
                        this,
                        "Device does not support scheduling exact alarms",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: SecurityException) {
                // Handle SecurityException if thrown
                e.printStackTrace()
                Toast.makeText(this, "SecurityException: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // For devices before Android 12, use setExact without checking
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                dateTime.timeInMillis,
                alarmIntent
            )
            Toast.makeText(
                this,
                "Alaram Set Successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }*/
    private fun scheduleNightlyUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val nightlyUpdateRequest = PeriodicWorkRequestBuilder<NightlyUpdateWorker>(
            repeatInterval = 1, // Repeat every 1 day
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(nightlyUpdateRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the user granted notification permission after opening settings
            checkNotificationPermission()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
              //  showNotificationPermissionDialog()
                showNotificationInstructionDialog()
            }
        }
    }

    // Go to Setting and Enable Notification
    private fun showNotificationPermissionDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Notification Permission")
            .setMessage("Please enable notifications to receive alarms.")
            .setPositiveButton("Go to settings") { dialogInterface: DialogInterface, _: Int ->
                openNotificationSettings()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                showToast("Notification permission denied.")
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    // Go to Setting and Enable Notification

    private fun showNotificationInstructionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("To receive timely updates, please enable notifications for this app.")
            .setPositiveButton("OK") { _, _ ->
                openNotificationSettings()
            }
            .show()
    }

    private fun openNotificationSettings() {
        val intent = Intent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", packageName)
                intent.putExtra("app_uid", applicationInfo.uid)
            }
            else -> {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:$packageName")
            }
        }
        startActivity(intent)
    }


 /*   private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, PERMISSION_REQUEST_CODE)
    }*/

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
