package com.example.android.happybirthdates.contacttracker

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log

import androidx.core.content.ContextCompat


private const val TAG = "ConStatNotfnBkgrndServ"

class ContactStatusNotificationBackgroundService : Service() {

    //--------------------------- Notification -----------------------------------------------------
    //-------------------- (c) AlarmManager.
    //---------- Technical (v)s for Notifications.
    private val REQUEST_CODE = 0
    private var alarmManager : AlarmManager? = null
    var notifyPendingIntent: PendingIntent? = null

    //---------- (v) for Push Notifications.
    private var mNotificationManager: NotificationManager? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "(m) onStartCommand.  Service started by user. Received start id $startId: $intent") ///<-> Toast.makeText(this, "(m) onStartCommand. Service started by user.", Toast.LENGTH_LONG).show()


        //---------- Technical (v) mContext of app.( (c) Intent & (c) NotificationManager ) <- (c) Context.
        var mContext = applicationContext

        //-------------------- Notification.
        //---------- (c) NotificationManager.
        mNotificationManager = ContextCompat.getSystemService(mContext!!, NotificationManager::class.java)

        //-------------------- Alarm.
        //---------- (c) AlarmManager.
        alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)

        //---------- (c) AlarmManager <- (c) AlarmReceiver, i.e. ((v) notifyPendingIntent <- (v) notifyIntent).
        val notifyIntent = Intent(this, AlarmReceiver::class.java) // IMPORTANT! Here's connection between (c) AlarmManager & (c) AlarmReceiver.
        notifyPendingIntent = PendingIntent.getBroadcast(mContext, REQUEST_CODE, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //---------- Technical (v) alarmManager Service. Start (c) AlarmManager Service.
        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HALF_DAY, notifyPendingIntent) //BY TESTING. AlarmManager.INTERVAL_HALF_DAY <-> 5000

        return START_NOT_STICKY
        //--------------------
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {

        Log.i(TAG, "(m) onDestroy. Service stopped.") ///<->Toast.makeText(this, "(m) onDestroy. Service stopped.", Toast.LENGTH_LONG).show()

        //-------------------- Alarm.
        //---------- (c) AlarmManager Service. Turn off.
        alarmManager?.cancel(notifyPendingIntent)
        //--------------------

        //-------------------- Notification.
        //---------- (c) NotificationManager Service. Turn off.
        mNotificationManager!!.cancelAll()
        //--------------------
    }


}
