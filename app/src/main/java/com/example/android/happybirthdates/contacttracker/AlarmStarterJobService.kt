package com.example.android.happybirthdates.contacttracker

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat



@SuppressLint("SpecifyJobSchedulerIdRange")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AlarmStarterJobService : JobService() {

    companion object {
        private const val TAG = "AlarmStarterJobService"
        private const val ALARM_REQUEST_CODE = 1111
        private const val PARAMETER_KEY = "notificationFrequency"
    }

    //--------------------------- Notification -----------------------------------------------------
    //-------------------- (c) AlarmManager.
    //---------- Technical (v)s for Notifications.

    private var alarmManager : AlarmManager? = null
    var pendingIntent: PendingIntent? = null

    // Start background task here
    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "CustomJobService onStartJob")


        val intervalMillis = params?.extras?.getString(PARAMETER_KEY)
        /*
        intervalMillis?.let {
            Toast.makeText(applicationContext, "Notification service (re)started", Toast.LENGTH_SHORT).show()
        }
        */
        //---------- Technical (v) mContext of app.( (c) Intent & (c) NotificationManager ) <- (c) Context.
        var mContext = applicationContext

        //-------------------- Alarm.
        //---------- (c) AlarmManager.
        alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)

        //---------- (c) AlarmManager <- (c) AlarmReceiver, i.e. ((v) notifyPendingIntent <- (v) notifyIntent).
        val intent = Intent(this, AlarmReceiver::class.java) // IMPORTANT! Here's connection between (c) AlarmManager & (c) AlarmReceiver.
        pendingIntent = PendingIntent.getBroadcast(mContext, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE)

        //---------- Technical (v) alarmManager Service. Start (c) AlarmManager Service.
        if (intervalMillis != null) {
            alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), intervalMillis.toLong(), pendingIntent)
        } //BY TESTING. AlarmManager.INTERVAL_HALF_DAY <-> 5000

        // Return true if the job needs to continue running in the background
        return true
    }

    // Stop any ongoing tasks or clean up resources
    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "CustomJobService onStopJob")

        //-------------------- Alarm.
        //---------- (c) AlarmManager Service. Turn off.
        alarmManager?.cancel(pendingIntent)
        pendingIntent!!.cancel()

        // Return true to reschedule the job if it needs to be run again
        return true
    }


}