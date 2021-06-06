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

package com.example.android.trackmysleepquality.contacttracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.ContactDatabase
import com.example.android.trackmysleepquality.databinding.FragmentContactTrackerBinding
import com.google.android.material.snackbar.Snackbar


/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class ContactTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- ContactTrackerActivity
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentContactTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_contact_tracker, container, false)

        val application = requireNotNull(this.activity).application

        //---------- ContactDao
        val dataSource = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- ContactTrackerViewModel
        val viewModelFactory = ContactTrackerViewModelFactory(dataSource, application)

        // Get a reference to the ViewModel associated with this fragment.
        val contactTrackerViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(ContactTrackerViewModel::class.java)



        //--------------------------- Processing ---------------------------------------------------
        binding.contactTrackerViewModel = contactTrackerViewModel

        //binding.setLifecycleOwner(this)
        binding.lifecycleOwner = this


        //-------------------- Start
        //---------- Observer; 'Start' button; Navigating.
        // Add an Observer on the state variable for Navigating when 'Start' button is pressed.
        contactTrackerViewModel.navigateToContactCreator.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(
                    ContactTrackerFragmentDirections
                        .actionContactTrackerFragmentToContactCreatorFragment())

                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                contactTrackerViewModel.doneNavigatingToContactCreatorFragment()
            }
        })


        //-------------------- Sleep
        //---------- Observer; 'Sleep' recyclerView ('Sleep' icons grid).
        val manager = GridLayoutManager(activity, 3)

        binding.sleepList.layoutManager = manager

        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =  when (position) {
                0 -> 3
                else -> 1
            }
        }

        val adapter = SleepNightAdapter(SleepNightListener { contactId ->
            contactTrackerViewModel.onContactClicked(contactId)
        })

        binding.sleepList.adapter = adapter

        contactTrackerViewModel.persons.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
            }
        })

        //---------- Observer; 'Sleep' icon; Navigating.
        contactTrackerViewModel.navigateToContactDetails.observe(viewLifecycleOwner, Observer { contactId ->
            contactId?.let {
                this.findNavController().navigate(
                    ContactTrackerFragmentDirections
                                .actionSleepTrackerFragmentToSleepDetailFragment(contactId))
                contactTrackerViewModel.doneNavigatingToContactDetailsFragment()
            }
        })


        //-------------------- Clear
        //---------- Observer; 'Clear' button; Snackbar.
        // Add an Observer on the state var showing a Snackbar msg when 'Clear' is pressed.
        contactTrackerViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                // Reset state to make sure the snackbar is only shown once, even if the device
                // has a configuration change.
                contactTrackerViewModel.doneShowingSnackbar()
            }
        })



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }
}
