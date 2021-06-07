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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.ContactDatabase
import com.example.android.trackmysleepquality.databinding.FragmentContactCreatorBinding


class ContactCreatorFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- <xml> |fragment| fragment_contact_creator
        val binding: FragmentContactCreatorBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_contact_creator, container, false)

        //---------- Technical (v) application
        val application = requireNotNull(this.activity).application


        //----------  |DB| Contact
        val dataSource = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactCreatorViewModel
        val viewModelFactory = ContactCreatorViewModelFactory(dataSource, application)
        val contactCreatorViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(ContactCreatorViewModel::class.java)



        //--------------------------- Processing ---------------------------------------------------
        binding.contactCreatorViewModel = contactCreatorViewModel

        //---------- Click listener; <tag> EditText & <Button> 'Submit'.
        binding.submitButton.setOnClickListener {
            binding.apply {
                contactCreatorViewModel.onCreateContact(binding.nameEdit.text.toString())
            }
        }

        //---------- Observer; <Button> 'Submit'; Navigating.
        // Add an Observer to the state variable for Navigating when the 'Submit' close_button is tapped.
        contactCreatorViewModel.navigateToContactTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    ContactCreatorFragmentDirections.actionContactCreatorFragmentToContactTrackerFragment())
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                contactCreatorViewModel.doneNavigating()
            }
        })



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }
}






















