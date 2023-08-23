package com.example.android.happybirthdates.contacttracker

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.happybirthdates.database.ContactDatabaseDao

/**
 * Boiler plate code for ViewModel Factory.
 *
 * Contact status (v), ContactDatabaseDao and context -> ViewModel.
 */
class ContactTrackerViewModelFactory constructor(private val isContactDeleted : Boolean, private val dataSource: ContactDatabaseDao, private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactTrackerViewModel::class.java)) {
            return ContactTrackerViewModel(isContactDeleted, dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

