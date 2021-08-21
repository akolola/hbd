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

package com.example.android.happybirthdates.contactdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactDetailsBinding


/**
 * A fragment with Contact details
 *
 */
class ContactDetailsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- <xml> |fragment| fragment_contact_details
        val binding: FragmentContactDetailsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_contact_details, container, false)

        //---------- Technical (v) application & (v) arguments
        val application = requireNotNull(this.activity).application
        val arguments = ContactDetailsFragmentArgs.fromBundle(arguments!!)

        //---------- |DB| Contact
        val dataSource = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactDetailsViewModel
        val viewModelFactory = ContactDetailsViewModelFactory(arguments.contactPersonKey, dataSource)
        val contactDetailsViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(ContactDetailsViewModel::class.java)



        //--------------------------- Processing ---------------------------------------------------
        binding.contactDetailsViewModel = contactDetailsViewModel
        binding.lifecycleOwner = this       // binding.setLifecycleOwner(this)

        //---------- Observer;  <Button> 'Clear'; Navigating.
        contactDetailsViewModel.navigateToContactTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    ContactDetailsFragmentDirections.actionContactDetailsFragmentToContactTrackerFragment())
                contactDetailsViewModel.doneNavigatingToContactTrackerFragment()
            }
        })



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }
}
