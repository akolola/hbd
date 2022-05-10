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
    private val NOTIFICATION_ID = 0
    private var alarmManager : AlarmManager? = null
    var notifyPendingIntent: PendingIntent? = null

    //--------------------------- Notification -----------------------------------------------------
    //---------- (v) for Push Notifications.
    private val PRIMARY_CHANNEL_ID = "primary_notification_channel"
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
        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyPendingIntent = PendingIntent.getBroadcast(mContext, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //---------- Technical (v) alarmManager Service. Start (c) AlarmManager Service.
        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 5000, notifyPendingIntent) //AlarmManager.INTERVAL_HALF_DAY

        return START_NOT_STICKY

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {

        Log.i(TAG, "(m) onDestroy. Service stopped.") ///<->Toast.makeText(this, "(m) onDestroy. Service stopped.", Toast.LENGTH_LONG).show()

        //-------------------- Alarm.
        //---------- (c) AlarmManager. Turn off Service.
        alarmManager?.cancel(notifyPendingIntent)

        //-------------------- Notification.
        //---------- (c) NotificationManager. Turn off Service.
        mNotificationManager!!.cancelAll()



    }



}
