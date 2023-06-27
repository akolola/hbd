package com.example.android.happybirthdates.contactcreator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.happybirthdates.database.ContactDatabaseDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Contact key (v), ContactDatabaseDao and context -> ViewModel.
 */
class ContactCreatorViewModelFactory(private val contactKey: Long, private val dataSource: ContactDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactCreatorViewModel::class.java)) {
            return ContactCreatorViewModel(contactKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
