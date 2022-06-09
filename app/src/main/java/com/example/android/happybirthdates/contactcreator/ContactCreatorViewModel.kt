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

package com.example.android.happybirthdates.contactcreator

import androidx.lifecycle.*
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.ContactPerson
import kotlinx.coroutines.*


/**
 * (c) ContactCreatorFragment's ViewModel.
 *
 * @param database (o) to |DB| where we get info about (c) Contact.
 */
class ContactCreatorViewModel constructor (private val contactKey: Long = 0L, val database: ContactDatabaseDao) : ViewModel() {


    //--------------------------- LiveData: <-(o) Person- DB ---------------------------------------
    //-------------------- LiveData preparation.
    //---------- (v) ldContact.
    var ldContact = MediatorLiveData<ContactPerson>()
    fun getContact() = ldContact
    init {
        // (c) MediatorLiveData to observe other (o)s LiveData & react to their onChange events
        ldContact.addSource(database.getContactWithId(contactKey), ldContact::setValue)
    }


    //-------------------- |DB| query (m)s.
    private suspend fun getLatestPersonFromDb(): ContactPerson? {
        return database.getLatestContact()
    }


    private suspend fun insertContactIntoDb(person: ContactPerson) {
        withContext(Dispatchers.IO) {
            database.insertContact(person)
        }
    }

    private suspend fun updatePersonInDb(person: ContactPerson) {
        withContext(Dispatchers.IO) {
            database.updateContact(person)
        }
    }




    //--------------------------- Buttons ----------------------------------------------------------
    //-------------------- Execution.
    //----------  <Button> 'Create' buttonClose is clicked.
    fun onCreateContact(name: String, birthDate: String, imageNameId: String) {
        viewModelScope.launch {

            //--- 1
            //- A. Creation Mode, contactKey (v) == null.
            val newPerson = ContactPerson()
            insertContactIntoDb(newPerson)

            //--- 2
            //- A. Creation Mode, contactKey (v) != null.
            ldContact.value = getLatestPersonFromDb()
            //- B. Edit Mode.
            ///person.value = getPersonFromDatabaseByID()
            //- Check (v).
            val liveDataPerson = ldContact.value ?: return@launch

            //--- 3
            liveDataPerson.name = name
            liveDataPerson.birthDate = birthDate
            liveDataPerson.imageNameId = imageNameId
            updatePersonInDb(liveDataPerson)

            //--- 4
            // Set '(v) = true' --> Observer &  -> Navigation.
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

