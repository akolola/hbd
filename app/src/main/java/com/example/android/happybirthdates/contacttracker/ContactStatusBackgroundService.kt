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

class ContactStatusBackgroundService() : Service() {

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

        Log.i(TAG, "Received start id $startId: $intent")
        Toast.makeText(this, "(m) onStartCommand. Service started by user.", Toast.LENGTH_LONG).show()

        //---------- (v) for Push Notifications.
        //--- (c) AlarmManager Service
        alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)

        //--- (v) notifyPendingIntent <-(v) notifyIntent <- (v) Birthday Contacts list.
        val msgList : ArrayList<String> = arrayListOf()
        msgList.add("John Duck")

        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyIntent.putStringArrayListExtra("MsgArrayList", msgList)

        var mContext = getApplicationContext()
        notifyPendingIntent = PendingIntent.getBroadcast(mContext, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //- (c) AlarmManager
        ///val repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES
        val triggerTime = (SystemClock.elapsedRealtime()) ///+ repeatInterval)
        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime,5000  , notifyPendingIntent) //repeatInterval, notifyPendingIntent)

        /*
        //---------- |DB| ContactDatabase -> (c) ContactStatusBackgroundService.
        var dbPerson = ContactDatabase.getInstance(application).contactDatabaseDao
        //--------------------------- LiveData: <-(o) Person- DB ---------------------------------------
        //---------- (v) person.
        var person = MutableLiveData<ContactPerson?>()
        val serviceJob = SupervisorJob()
        val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
        checkBirthdayPeople(serviceScope, person, dbPerson)
        */

        return START_NOT_STICKY

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {

        Toast.makeText(this, "(m) onDestroy. Service stopped.", Toast.LENGTH_LONG).show()

        //- (c) AlarmManager off.
        alarmManager?.cancel(notifyPendingIntent)

    }
    /*
    private fun checkBirthdayPeople(serviceScope: CoroutineScope, person: MutableLiveData<ContactPerson?>, dbPerson: ContactDatabaseDao) {
        serviceScope.launch {
            person.value = getPersonFromDatabase(dbPerson)
            Log.i(TAG, "DB req res = "+ (person.value?.name ?: "EMPTY"))
        }
    }
    */

    //-------------------- DB query (m)s.
    private suspend fun getPersonFromDatabase(dbPerson: ContactDatabaseDao): ContactPerson? {
        return dbPerson.getPerson()
    }


}