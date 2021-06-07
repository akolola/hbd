/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.contactcreator

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.ContactDatabaseDao
import com.example.android.trackmysleepquality.database.ContactPerson
import kotlinx.coroutines.*


/**
 *  ContactCreatorFragment's ViewModel.
 */
class ContactCreatorViewModel(val database: ContactDatabaseDao, application: Application) : ViewModel() {


    //--------------------------- LiveData: <-(o) Person- DB ---------------------------------------
    //-------------------- LiveData preparation
    //---------- <list> persons
    val persons = database.getAllPersons()

    //---------- (v) person
    private var person = MutableLiveData<ContactPerson?>()
    init {
        initializePerson()
    }
    private fun initializePerson() {
        viewModelScope.launch {
            person.value = getPersonFromDatabase()
        }
    }

    //-------------------- Query (m)s
    //---------- (m) Get
    private suspend fun getPersonFromDatabase(): ContactPerson? {
        var person = database.getPerson()
        if (person?.endTimeMilli != person?.startTimeMilli) {
            person = null
        }
        return person
    }

    //---------- (m)s remaining
    private suspend fun insert(person: ContactPerson) {
        withContext(Dispatchers.IO) {
            database.insert(person)
        }
    }

    private suspend fun update(person: ContactPerson) {
        withContext(Dispatchers.IO) {
            database.update(person)
        }
    }



    //--------------------------- Buttons ----------------------------------------------------------
    //-------------------- Execution
    //----------  <Button> 'Create' close_button is clicked.
    fun onCreateContact(name: String) {
        viewModelScope.launch {

            //--- 1
            val newPerson = ContactPerson()
            insert(newPerson)

            //--- 2
            person.value = getPersonFromDatabase()
            val liveDataPerson = person.value ?: return@launch

            //--- 3
            liveDataPerson.endTimeMilli = System.currentTimeMillis()
            liveDataPerson.name = name
            update(liveDataPerson)

            //--- 4
            // Setting this state variable to true will alert the observer and trigger navigation.
            _navigateToContactTracker.value = true

        }
    }


    //-------------------- Navigation
    //---------- ContactCreatorFragment => ContactTrackerFragment
    private val _navigateToContactTracker = MutableLiveData<Boolean?>()

    val navigateToContactTracker: LiveData<Boolean?>
        get() = _navigateToContactTracker

    fun doneNavigating() {
        _navigateToContactTracker.value = null
    }

}

