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

package com.example.android.happybirthdates.contactdetails

import androidx.lifecycle.*
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.ContactPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ContactDetailsFragment's ViewModel.
 *
 * @param contactKey The key of the current (c) Contact we are working on.
 * @param database (o) to |DB| where we get info about (c) Contact.
 */
class ContactDetailsViewModel constructor(private val contactKey: Long = 0L, val database: ContactDatabaseDao) : ViewModel() {

    //--------------------------- LiveData: <-(o) ContactPerson- DB --------------------------------
    //-------------------- (c) MediatorLiveData preparation.
    //---------- (c) MediatorLiveData.
    val ldPerson = MediatorLiveData<ContactPerson>()
    fun getPerson() = ldPerson

    //---------- |DB| Contact.
    val dbPerson = database

    init {
        // (c) MediatorLiveData to observe other (o)s LiveData & react to their onChange events
        ldPerson.addSource(dbPerson.getContactWithId(contactKey), ldPerson::setValue)
    }

    //-------------------- Query (m)s
    //---------- (m) clear
    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            database.delete()
        }
    }


    //--------------------------- Buttons ----------------------------------------------------------
    //-------------------- Execution
    //----------  <Button> 'Close' is clicked.
    fun onClose() {
        _navigateToContactTracker.value = true
    }

    //----------  <Button> 'Delete' is clicked.
    fun onDelete() {
        viewModelScope.launch {
            // Clear the database table.
            delete()
            // And clear (o) Person since it's no longer in the DB
            ldPerson.value = null
        }

        //---- Navigation
        _navigateToContactTracker.value = true
    }



    //-------------------- Navigation
    //---------- ContactDetailsFragment => ContactTrackerFragment.
    private val _navigateToContactTracker = MutableLiveData<Boolean?>()

    val navigateToContactTracker: LiveData<Boolean?>
        get() = _navigateToContactTracker

    fun doneNavigatingToContactTrackerFragment() {
        _navigateToContactTracker.value = null
    }

}

 