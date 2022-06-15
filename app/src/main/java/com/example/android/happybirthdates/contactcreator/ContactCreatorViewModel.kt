package com.example.android.happybirthdates.contactcreator

import androidx.lifecycle.*
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.Contact
import kotlinx.coroutines.*


/**
 * (c) ContactCreatorFragment's ViewModel.
 *
 * @param database (o) to |DB| where we get info about (c) Contact.
 */
class ContactCreatorViewModel constructor (private val contactKey: Long = 0L, val database: ContactDatabaseDao) : ViewModel() {


    //--------------------------- LiveData: <-(o) Person- |DB| -------------------------------------
    //-------------------- LiveData preparation.
    //---------- (v) ldContact.
    var liveDataContact = MediatorLiveData<Contact>()
    //--- (c) MediatorLiveData to observe other (o)s LiveData & react to their onChange events
    init { liveDataContact.addSource(database.getContactWithId(contactKey), liveDataContact::setValue) }
    //--------------------


    //--------------------------- |DB| query (m)s --------------------------------------------------
    private suspend fun getLatestContactFromDb(): Contact? {
        return database.getLatestContact()
    }

    private suspend fun getContactByIdFromDb(contactId: Long): Contact? {
        return database.getContactWithIdNotLiveData(contactId)
    }


    private suspend fun insertContactIntoDb(person: Contact) {
        withContext(Dispatchers.IO) {
            database.insertContact(person)
        }
    }

    private suspend fun updateContactInDb(person: Contact) {
        withContext(Dispatchers.IO) {
            database.updateContact(person)
        }
    }



    //--------------------------- GUI Elements -----------------------------------------------------
    //-------------------- 'editTextName' <EditText> & 'textViewBirthdate' <TextView>.
    fun getContact() = liveDataContact
    //--------------------

    //-------------------- 'Create' <Button>.
    fun onCreateContact(contactId: Long, name: String, birthDate: String, imageNameId: String) {
        viewModelScope.launch {

            //--- 1
            if(contactId == 0L){
            //- A. Creation Mode.
                insertContactIntoDb(Contact())
                liveDataContact.value = getLatestContactFromDb()
            }
            else{
            //- B. Edit Mode.
                liveDataContact.value = getContactByIdFromDb(contactId)
            }
            //- Check (v).
            val liveDataContact = liveDataContact.value ?: return@launch

            //--- 2
            liveDataContact.name = name                                     // May be updated with empty string ""
            liveDataContact.birthDate = birthDate                           // May be updated with empty string ""
            if(imageNameId != "") liveDataContact.imageId = imageNameId // May NOT be updated with empty string ""
            updateContactInDb(liveDataContact)

            //--- 3
            // Set '(v) = true' --> Observer &  -> Navigation.
            _navigateToContactTracker.value = true

        }
    }
    //--------------------



    //--------------------------- Navigation -------------------------------------------------------
    //-------------------- ContactCreatorFragment => ContactTrackerFragment.
    private val _navigateToContactTracker = MutableLiveData<Boolean?>()
    val navigateToContactTracker: LiveData<Boolean?>
    get() = _navigateToContactTracker
    fun doneNavigating() {
        _navigateToContactTracker.value = null
    }
    //--------------------



}

