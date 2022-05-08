package com.example.android.happybirthdates.contacttracker

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.ContactPerson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "ContactStatusBkgrndServ"

class ContactStatusBackgroundService : Service() {

    //---------- (v)s for Push Notifications.
    private val NOTIFICATION_ID = 0
    var  alarmManager : AlarmManager? = null
    // notifyIntent.    <- (o) DAO
    var notifyPendingIntent: PendingIntent? = null

    /**
     * Class for clients to access.
     * Return running service instance in binder.
     */
    inner class LocalBinder : Binder() {
        fun getService(): ContactStatusBackgroundService? {
            return this@ContactStatusBackgroundService
        }
    }

    override fun onCreate() {
        Toast.makeText(this, "(m) onCreate. Service created.", Toast.LENGTH_LONG).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "(m)onStartCommand.  Service started by user. Received start id $startId: $intent") ///<-> Toast.makeText(this, "(m) onStartCommand. Service started by user.", Toast.LENGTH_LONG).show()

        //---------- (v) for Push Notifications.
        //--- (c) AlarmManager Service
        alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)

        //--- (v) notifyPendingIntent <-(v) notifyIntent <- (v) Birthday Contacts list.
        val msgList : ArrayList<String> = arrayListOf()
        msgList.add("John Duck")

        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyIntent.putStringArrayListExtra("MsgArrayList", msgList)

        var mContext = applicationContext
        notifyPendingIntent = PendingIntent.getBroadcast(mContext, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //- (c) AlarmManager
        ///val repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES
        val triggerTime = (SystemClock.elapsedRealtime()) ///+ repeatInterval)
        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime,5000  , notifyPendingIntent) //repeatInterval, notifyPendingIntent)

        return START_NOT_STICKY

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {

        Log.i(TAG, "(m)onDestroy. Service stopped.") ///<->Toast.makeText(this, "(m) onDestroy. Service stopped.", Toast.LENGTH_LONG).show()

        //- (c) AlarmManager off.
        alarmManager?.cancel(notifyPendingIntent)

    }


}