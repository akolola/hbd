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

package com.example.android.trackmysleepquality.contacttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.ContactDatabaseDao
import com.example.android.trackmysleepquality.database.ContactPerson
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*
import androidx.lifecycle.viewModelScope

/**
 * ViewModel for ContactTrackerViewModel.
 */
class ContactTrackerViewModel(
    val database: ContactDatabaseDao,
    application: Application) : AndroidViewModel(application) {


    //---------------------------  <-Person- DB  ---------------------------
    private var person = MutableLiveData<ContactPerson?>()

    val persons = database.getAllPersons()

    /**
     * Converted persons to Spanned for displaying.
     */
    val personsString = Transformations.map(persons) { persons ->
        formatNights(persons, application.resources)
    }



    //--------------------------- Buttons visibility---------------------------
    /**
     * If person has not been set, then the START button should be visible.
     */
    val startButtonVisible = Transformations.map(person) {
        null == it
    }

    /**
     * If person has been set, then the STOP button should be visible.
     */
    val stopButtonVisible = Transformations.map(person) {
        null != it
    }

    /**
     * If there are any persons in the database, show the CLEAR button.
     */
    val clearButtonVisible = Transformations.map(persons) {
        it?.isNotEmpty()
    }





    //--------------------------- Snackbar ---------------------------
    /**
     * Request a toast by setting this value to true.
     */
    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    /**
     * If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.
     */
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    /**
     * Call this immediately after calling `show()` on a toast.
     *
     * It will clear the toast request, so if the user rotates their phone it won't show a duplicate
     * toast.
     */
    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }


    //--------------------------- Navigation ---------------------------
    /**
     * Variable that tells the Fragment to navigate to a specific [ContactCreatorFragment]
     */
    private val _navigateToContactCreator = MutableLiveData<ContactPerson>()

    /**
     * If this is non-null, immediately navigate to [ContactCreatorFragment] and call [doneNavigating]
     */
    val navigateToSleepQuality: LiveData<ContactPerson>
        get() = _navigateToContactCreator

    /**
     * Call this immediately after navigating to [ContactCreatorFragment]
     *
     * It will clear the navigation request, so if the user rotates their phone it won't navigate
     * twice.
     */
    fun doneNavigating() {
        _navigateToContactCreator.value = null
    }

    private val _navigateToContactCreatorData = MutableLiveData<Long>()
    val navigateToSleepDataQuality
        get() = _navigateToContactCreatorData

    fun onSleepNightClicked(id: Long) {
        _navigateToContactCreatorData.value = id
    }

    fun onContactCreatorDataNavigated() {
        _navigateToContactCreatorData.value = null
    }



    //--------------------------- DB ---------------------------
    init {
        initializePerson()
    }

    private fun initializePerson() {
        viewModelScope.launch {
            person.value = getPersonFromDatabase()
        }
    }

    /**
     *  Handling the case of the stopped app or forgotten recording,
     *  the start and end times will be the same.j
     *
     *  If the start time and end time are not the same, then we do not have an unfinished
     *  recording.
     */
    private suspend fun getPersonFromDatabase(): ContactPerson? {
        //return withContext(Dispatchers.IO) {
            var person = database.getPerson()
            if (person?.endTimeMilli != person?.startTimeMilli) {
                person = null
            }
            return person
        //}
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    private suspend fun update(person: ContactPerson) {
        withContext(Dispatchers.IO) {
            database.update(person)
        }
    }

    private suspend fun insert(person: ContactPerson) {
        withContext(Dispatchers.IO) {
            database.insert(person)
        }
    }


    //--------------------------- Buttons executions ---------------------------
    /**
     * Executes when the START button is clicked.
     */
    fun onStartTracking() {
        viewModelScope.launch {
            // Create a new person, which captures the current time,
            // and insert it into the database.
            val newPerson = ContactPerson()

            insert(newPerson)

            person.value = getPersonFromDatabase()
        }
    }

    /**
     * Executes when the STOP button is clicked.
     */
    fun onStopTracking() {
        viewModelScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val oldPerson = person.value ?: return@launch

            // Update the person in the database to add the end time.
            oldPerson.endTimeMilli = System.currentTimeMillis()

            update(oldPerson)

            // Set state to navigate to the SleepQualityFragment.
            _navigateToContactCreator.value = oldPerson
        }
    }

    /**
     * Executes when the CLEAR button is clicked.
     */
    fun onClear() {
        viewModelScope.launch {
            // Clear the database table.
            clear()

            // And clear person since it's no longer in the database
            person.value = null
        }

        // Show a snackbar message, because it's friendly.
        _showSnackbarEvent.value = true
    }

}
