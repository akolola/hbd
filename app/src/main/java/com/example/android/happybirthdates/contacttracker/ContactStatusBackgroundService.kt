package com.example.android.happybirthdates.contacttracker

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.android.happybirthdates.database.ContactDatabase

private const val TAG = "ContactStatusBackgroundService"

class ContactStatusBackgroundService : Service() {

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
        //---------- Technical (v) application
        val application = requireNotNull(this).application
        var dbPerson = ContactDatabase.getInstance(application).contactDatabaseDao
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Received start id $startId: $intent")
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    // The (o) that receives interactions from clients.
    private val mBinder: IBinder = LocalBinder()

    /*
      //--------------------------- LiveData: <-(o) Person- DB ---------------------------------------
      //-------------------- LiveData preparation.
      //---------- (v) person.
       private var person = MutableLiveData<ContactPerson?>()
       private val serviceJob = SupervisorJob()
       private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

       init {
          checkBirthdayPeople()
       }

          fun checkBirthdayPeople() {
              serviceScope.launch {
                  person.value = getPersonFromDatabase()
              }
          }

         //-------------------- DB query (m)s.
         private suspend fun getPersonFromDatabase(): ContactPerson? {
             return dbPerson.getPerson()
         }
    */

}