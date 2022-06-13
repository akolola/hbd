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


    //--------------------------- LiveData: <-(o) ContactPerson- |DB| ------------------------------
    //-------------------- (c) MediatorLiveData preparation.
    //---------- (c) MediatorLiveData.
    val ldContact = MediatorLiveData<ContactPerson>()
    //--- (c) MediatorLiveData to observe other (o)s LiveData & react to their onChange events
    init { ldContact.addSource(database.getContactWithId(contactKey), ldContact::setValue) }
    //--------------------



    //--------------------------- |DB| query (m)s --------------------------------------------------
    private suspend fun deleteContact(contactPersonKey: Long) {
        withContext(Dispatchers.IO) {
            database.deleteContactsById(contactPersonKey)
        }
    }



    //--------------------------- GUI Elements -----------------------------------------------------
    //-------------------- 'EditTextName' <EditText> & 'TextViewBirthdate' <TextView>.
    fun getContact() = ldContact
    //--------------------

    //-------------------- 'Edit' <Button>.
    fun onEditContact() {
        _navigateToContactCreator.value = contactKey
    }

    //-------------------- 'Close' <Button>.
    fun onCloseContactDetails() {
        _navigateToContactTracker.value = true
    }

    //-------------------- 'Delete' <Button>.
    fun onDeleteContact(contactPersonKey: Long) {
        viewModelScope.launch {

            //--- Clear in |DB|.
            deleteContact(contactPersonKey)
            //--- And clear (o) Contact since it's no longer in |DB|.
            ldContact.value = null
        }

        _navigateToContactTracker.value = true
    }
    //--------------------



    //--------------------------- Navigation -------------------------------------------------------
    //--------------------ContactDetailsFragment => ContactTrackerFragment.
    private val _navigateToContactTracker = MutableLiveData<Boolean?>()
    val navigateToContactTracker: LiveData<Boolean?>
    get() = _navigateToContactTracker
    fun doneNavigatingToContactTrackerFragment() {
        _navigateToContactTracker.value = null
    }
    //--------------------

    //-------------------- ContactDetailsFragment (v)-> => ContactCreatorFragment.
    private val _navigateToContactCreator = MutableLiveData<Long?>()
    val navigateToContactCreator: LiveData<Long?>
    get() = _navigateToContactCreator
    fun doneNavigatingToContactCreatorFragment() {
        _navigateToContactCreator.value = null
    }
    //--------------------

}

 