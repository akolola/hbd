package com.example.android.happybirthdates.contacttracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log

import androidx.core.content.ContextCompat


private const val TAG = "ConStatNotfnBkgrndServ"

class ContactStatusNotificationBackgroundService : Service() {

    //---------- Technical (v)s for Notifications.
    private val NOTIFICATION_ID = 0
    var alarmManager : AlarmManager? = null
    var notifyPendingIntent: PendingIntent? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "(m) onStartCommand.  Service started by user. Received start id $startId: $intent") ///<-> Toast.makeText(this, "(m) onStartCommand. Service started by user.", Toast.LENGTH_LONG).show()

        //---------- Technical (v) alarmManager Service. Assign val.
        alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)

        //---------- (v) notifyPendingIntent <- (v) notifyIntent.
        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        var mContext = applicationContext
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

        //---------- Technical (v) alarmManager Service. Turn off Service.
        alarmManager?.cancel(notifyPendingIntent)

    }


}