/*
 * Copyright 2021, The Android Open Source Project
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.ContactPerson

/**
 * ContactDetailsFragment's ViewModel.
 *
 * @param contactKey The key of the current (o) Contact we are working on.
 */
class ContactDetailsViewModel constructor(private val contactKey: Long = 0L, dataSource: ContactDatabaseDao) : ViewModel() {

    //--------------------------- LiveData: <-(o) Person- DB ---------------------------------------
    //-------------------- MediatorLiveData preparation.
    //---------- (v) ldPerson.
    val ldPerson = MediatorLiveData<ContactPerson>()

    fun getPerson() = ldPerson

    //---------- |DB| Contact.
    val dbPerson = dataSource


    init {
        // (c) MediatorLiveData to observe other (o)s LiveData & react to their onChange events
        ldPerson.addSource(dbPerson.getContactWithId(contactKey), ldPerson::setValue)

    }

    //--------------------------- Buttons ----------------------------------------------------------
    //-------------------- Execution
    //----------  <Button> 'Close' is clicked.
    fun onClose() {
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

 