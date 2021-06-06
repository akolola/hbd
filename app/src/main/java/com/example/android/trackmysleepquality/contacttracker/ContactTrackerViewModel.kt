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
import kotlinx.coroutines.*
import androidx.lifecycle.viewModelScope

/**
 * ViewModel for ContactTrackerViewModel.
 */
class ContactTrackerViewModel(
    val database: ContactDatabaseDao,
    application: Application) : AndroidViewModel(application) {



    //--------------------------- LiveData: <-(o) Person- DB ---------------------------------------
    //-------------------- LiveData preparation
    //---------- <list> persons
    val persons = database.getAllPersons()

    //---------- (v) person
    private var person = MutableLiveData<ContactPerson?>()


    //-------------------- Query (m)s
    //---------- (m) clear
    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }



    //--------------------------- Buttons ----------------------------------------------------------
    //-------------------- Visibility
    /** If person has been set, then the STOP button should be visible. */
    val stopButtonVisible = Transformations.map(person) {
        null != it
    }

    /** If there are any persons in the database, show the CLEAR button. */
    val clearButtonVisible = Transformations.map(persons) {
        it?.isNotEmpty()
    }


    //-------------------- Execution
    //---------- 'Create' button is clicked
    fun onCreateTracking() {
        viewModelScope.launch {

            _navigateToContactCreator.value = true

        }
    }

    //---------- 'Clear' button is clicked
    fun onClear() {
        viewModelScope.launch {
            // Clear the database table.
            clear()
            // And clear (o) Person since it's no longer in the DB
            person.value = null
        }
        // Show a snackbar msg, because it's friendly.
        _showSnackbarEvent.value = true
    }

    fun onStopTracking() {}


    //-------------------- Navigation
    //---------- ContactTrackerFragment => ContactCreatorFragment
    private val _navigateToContactCreator = MutableLiveData<Boolean?>()

    val navigateToContactCreator: LiveData<Boolean?>
        get() = _navigateToContactCreator

    fun doneNavigating() {
        _navigateToContactCreator.value = null
    }

    //---------- ContactTrackerFragment => ContactDetailsFragment
    private val _navigateToContactDetails = MutableLiveData<Long>()
    val navigateToContactDetails
        get() = _navigateToContactDetails


    fun onContactClicked(id: Long) {
        _navigateToContactDetails.value = id
    }

    fun onContactCreatorDataNavigated() {
        _navigateToContactDetails.value = null
    }



    //--------------------------- Snackbar ---------------------------------------------------------
    /** Request a toast by setting this value to true.     */
    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    /** If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.  */
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

}
