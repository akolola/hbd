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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.happybirthdates.database.ContactDatabaseDao

/**
 * Boiler plate code for ViewModel Factory.
 *
 * Contact status (v), ContactDatabaseDao and context -> ViewModel.
 */
class ContactTrackerViewModelFactory(private val isContactDeleted : Boolean, private val dataSource: ContactDatabaseDao, private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactTrackerViewModel::class.java)) {
            return ContactTrackerViewModel(isContactDeleted, dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

