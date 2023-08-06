package com.example.android.happybirthdates.contacttracker

import android.app.*
import android.content.Intent
import android.app.Service
import android.os.Handler
import android.os.IBinder

import android.os.SystemClock
import android.util.Log
import android.widget.Toast



import androidx.core.content.ContextCompat


private const val TAG = "ConStatNotfnBkgrndServ"

class ContactStatusNotificationBackgroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    private val restartDelay = 1000L // Delay in milliseconds before the service restarts
    private val handler = Handler()
    private lateinit var runnable: Runnable


    //--------------------------- Notification -----------------------------------------------------
    //-------------------- (c) AlarmManager.
    //---------- Technical (v)s for Notifications.
    private val REQUEST_CODE = 0
    private var alarmManager : AlarmManager? = null
    var pendingIntent: PendingIntent? = null

    //---------- (v) for Push Notifications.
    private var mNotificationManager: NotificationManager? = null




    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "(m) onCreate. Service created")
        Toast.makeText(this, "(m) onCreate. Service created.", Toast.LENGTH_LONG).show()

        // Create a runnable to restart the service
        runnable = Runnable {
            val intent = Intent(applicationContext, ContactStatusNotificationBackgroundService::class.java)
            startService(intent)
        }


    }






    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "(m) onStartCommand.  Service started by user. Received start id $startId: $intent") ///<-> Toast.makeText(this, "(m) onStartCommand. Service started by user.", Toast.LENGTH_LONG).show()
        Toast.makeText(this, "(m) onStartCommand. Service started.", Toast.LENGTH_LONG).show()


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


        //-------------------- (v) START_STICKY to ensure the service keeps running even if the system destroys and recreates it
        return START_STICKY


    }




    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "(m) onDestroy. Service destroyed.")
        Toast.makeText(this, "(m) onDestroy. Service destroyed.", Toast.LENGTH_LONG).show()


        //-------------------- Alarm.
        //---------- (c) AlarmManager Service. Turn off.
        //alarmManager?.cancel(pendingIntent)
        //--------------------
        //-------------------- Notification.
        //---------- (c) NotificationManager Service. Turn off.
        //mNotificationManager!!.cancelAll()


        //--------------------
        // Restart the service after a delay
        handler.postDelayed(runnable, restartDelay)


        // Stop the service and remove the notification
        stopForeground(true)
        stopSelf()

    }





    override fun onBind(intent: Intent?): IBinder? {
        // Return null because this service doesn't support binding
        return null
    }



}
