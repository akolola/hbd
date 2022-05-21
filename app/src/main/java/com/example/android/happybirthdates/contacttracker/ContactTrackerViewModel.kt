/*
 * Copyright 2022, The Android Open Source Project
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

package com.example.android.happybirthdates.contacttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.ContactPerson
import kotlinx.coroutines.*
import androidx.lifecycle.viewModelScope

/**
 * ContactTrackerFragment's ViewModel.
 */
class ContactTrackerViewModel constructor(val database: ContactDatabaseDao, application: Application) : AndroidViewModel(application) {



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
            database.delete()
        }
    }



    //--------------------------- Buttons ----------------------------------------------------------
    //-------------------- Visibility
    //----------  <Button> 'Create' is visible if there are any persons in the database.
    val clearButtonVisible = Transformations.map(persons) {
        it?.isNotEmpty()
    }


    //-------------------- Execution
    //----------  <Button> 'buttonCreate' is clicked.
    fun onCreateTracking() {
        viewModelScope.launch {
            _navigateToContactCreator.value = true
        }
    }

    //---------- <ImageView> 'imageViewContactPicture' is clicked.
    fun onContactClicked(contactId: Long) {
        _navigateToContactDetails.value = contactId
    }

    //----------  <Button> 'Clear' is clicked.
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


    //-------------------- Navigation
    //---------- (c) ContactTrackerFragment => (c) ContactCreatorFragment.
    private val _navigateToContactCreator = MutableLiveData<Boolean?>()

    val navigateToContactCreator: LiveData<Boolean?>
        get() = _navigateToContactCreator

    fun doneNavigatingToContactCreatorFragment() {
        _navigateToContactCreator.value = null
    }

    //---------- (c) ContactTrackerFragment => (c) ContactDetailsFragment.
    private val _navigateToContactDetails = MutableLiveData<Long>()

    val navigateToContactDetails
        get() = _navigateToContactDetails

    fun doneNavigatingToContactDetailsFragment() {
        _navigateToContactDetails.value = null
    }


    //--------------------------- Snackbar ---------------------------------------------------------
    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

}
