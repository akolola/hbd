package com.example.android.happybirthdates.contacttracker

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@SuppressLint("SpecifyJobSchedulerIdRange")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CustomJobService : JobService() {


    //--------------------------- Notification -----------------------------------------------------
    //-------------------- (c) AlarmManager.
    //---------- Technical (v)s for Notifications.
    private val REQUEST_CODE = 0
    private var alarmManager : AlarmManager? = null
    var pendingIntent: PendingIntent? = null

    //---------- (v) for Push Notifications.
    private var mNotificationManager: NotificationManager? = null

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "CustomJobService onStartJob")
        // Start your background task here



        //---------- Technical (v) mContext of app.( (c) Intent & (c) NotificationManager ) <- (c) Context.
        var mContext = applicationContext


        //-------------------- Notification.
        //---------- (c) NotificationManager.
        mNotificationManager = ContextCompat.getSystemService(mContext!!, NotificationManager::class.java)

        //-------------------- Alarm.
        //---------- (c) AlarmManager.
        alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)

        //---------- (c) AlarmManager <- (c) AlarmReceiver, i.e. ((v) notifyPendingIntent <- (v) notifyIntent).
        val intent = Intent(this, AlarmReceiver::class.java) // IMPORTANT! Here's connection between (c) AlarmManager & (c) AlarmReceiver.
        pendingIntent = PendingIntent.getBroadcast(mContext, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE)

        //---------- Technical (v) alarmManager Service. Start (c) AlarmManager Service.
        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 30000, pendingIntent) //BY TESTING. AlarmManager.INTERVAL_HALF_DAY <-> 5000









        // Return true if the job needs to continue running in the background
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "CustomJobService onStopJob")
        // Stop any ongoing tasks or clean up resources here


        //-------------------- Alarm.
        //---------- (c) AlarmManager Service. Turn off.
        alarmManager?.cancel(pendingIntent)
        //--------------------
        //-------------------- Notification.
        //---------- (c) NotificationManager Service. Turn off.
        mNotificationManager!!.cancelAll()


        // Return true to reschedule the job if it needs to be run again
        return true
    }

    companion object {
        private const val TAG = "CustomJobService"
    }
}