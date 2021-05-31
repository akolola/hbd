/*
 * Copyright 2018, The Android Open Source Project
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

package com.example.android.trackmysleepquality.contactcreator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.ContactDatabaseDao
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

/**
 * ViewModel for SleepQualityFragment.
 *
 * @param sleepNightKey The key of the current night we are working on.
 */
class ContactCreatorViewModel(
        private val sleepNightKey: Long = 0L,
        val database: ContactDatabaseDao) : ViewModel() {

    /*
    /** Coroutine setup variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = Job()

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this scope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [ViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    */

    /**
     * Variable that tells the fragment whether it should navigate to [ContactTrackerFragment].
     *
     * This is `private` because we don't want to expose the ability to set [MutableLiveData] to
     * the [Fragment]
     */
    private val _navigateToContactTracker = MutableLiveData<Boolean?>()

    /**  When true immediately navigate back to the [ContactTrackerFragment]     */
    val navigateToContactTracker: LiveData<Boolean?>
        get() = _navigateToContactTracker


    /**  Call this immediately after navigating to [ContactTrackerFragment]     */
    fun doneNavigating() {
        _navigateToContactTracker.value = null
    }

    /** Sets the name and updates th DB. Then navigates back to the ContactTrackerFragment.*/
    fun onSetName(name: String) {
        viewModelScope.launch {
            // IO is a thread pool for running operations that access the disk, such as
            // our Room database.
            val person = database.get(sleepNightKey) ?: return@launch
            person.name = name

            database.update(person)
        }
    }

    //--- Old
    /** Sets the sleep quality and updates th DB. Then navigates back to the ContactTrackerFragment.*/
    fun onSetSleepQuality(quality: Int) {
        viewModelScope.launch {
            val person = database.get(sleepNightKey) ?: return@launch
            person.sleepQuality = quality

            database.update(person)

            // Setting this state variable to true will alert the observer and trigger navigation.
            _navigateToContactTracker.value = true
        }
    }







}

