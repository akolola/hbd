package com.example.android.happybirthdates.contacttracker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.ContactPerson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ContactStatusService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

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
             //person.value = getPersonFromDatabase()
         }
     }

    //-------------------- DB query (m)s.
    /*
    private suspend fun getPersonFromDatabase(): ContactPerson? {
        return database.getPerson()
    }
    */
}