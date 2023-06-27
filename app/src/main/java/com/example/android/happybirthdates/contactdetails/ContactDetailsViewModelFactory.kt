package com.example.android.happybirthdates.contactdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.happybirthdates.database.ContactDatabaseDao

/**
 * (c) ViewModelFactory. Provides key for Contact and (c) ContactDatabaseDao to the ViewModel.
 *
 * Contact key (v), ContactDatabaseDao and context -> ViewModel.
 */
class ContactDetailsViewModelFactory(private val contactKey: Long, private val dataSource: ContactDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactDetailsViewModel::class.java)) {
            return ContactDetailsViewModel(contactKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

 