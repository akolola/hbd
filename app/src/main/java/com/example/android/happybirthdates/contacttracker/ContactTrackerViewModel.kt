package com.example.android.happybirthdates.contacttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.happybirthdates.database.ContactDatabaseDao
import kotlinx.coroutines.*
import androidx.lifecycle.viewModelScope

/**
 * ContactTrackerFragment's ViewModel.
 */
class ContactTrackerViewModel constructor(isContactDeleted : Boolean, val database: ContactDatabaseDao, application: Application) : AndroidViewModel(application) {



    //--------------------------- LiveData: <-(o) Person- DB ---------------------------------------
    //-------------------- LiveData preparation
    //---------- <list> persons
    val persons = database.getAllContacts()
    //--------------------



    //--------------------------- Buttons ----------------------------------------------------------
    //-------------------- Execution
    //----------  <Button> 'buttonCreate' is clicked.
    fun onCreateTracking() {
        viewModelScope.launch {
            _navigateToContactCreator.value = true
        }
    }

    fun onCreateIcTracking() {
        viewModelScope.launch {
            _navigateToImageCrop.value = true
        }
    }
    //--------------------
    //---------- <ImageView> 'imageViewContactPicture' is clicked.
    fun onContactClicked(contactId: Long) {
        _navigateToContactDetails.value = contactId
    }
    //--------------------
    //----------  <Button> 'buttonStorage' is clicked.
    fun onStorageTracking() {
        viewModelScope.launch {
            _navigateToContactStorage.value = true
        }
    }
    //--------------------



    //--------------------------- Navigation -------------------------------------------------------
    //---------- (c) ContactTrackerFragment => (c) ContactCreatorFragment.
    private val _navigateToContactCreator = MutableLiveData<Boolean?>()
    val navigateToContactCreator: LiveData<Boolean?>
        get() = _navigateToContactCreator
    fun doneNavigatingToContactCreatorFragment() {
        _navigateToContactCreator.value = null
    }

    //---------- (c) ContactTrackerFragment => (c) ImageCropFragment.
    private val _navigateToImageCrop = MutableLiveData<Boolean?>()
    val navigateToImageCroper: LiveData<Boolean?>
        get() = _navigateToImageCrop
    fun doneNavigatingToImageCropFragment() {
        _navigateToImageCrop.value = null
    }

    //---------- (c) ContactTrackerFragment => (c) ContactDetailsFragment.
    private val _navigateToContactDetails = MutableLiveData<Long>()
    val navigateToContactDetails
        get() = _navigateToContactDetails
    fun doneNavigatingToContactDetailsFragment() {
        _navigateToContactDetails.value = null
    }

    //---------- (c) ContactTrackerFragment => (c) ContactStorageFragment.
    private val _navigateToContactStorage = MutableLiveData<Boolean?>()
    val navigateToContactStorage: LiveData<Boolean?>
        get() = _navigateToContactStorage
    fun doneNavigatingToContactStorageFragment() {
        _navigateToContactStorage.value = null
    }




}
