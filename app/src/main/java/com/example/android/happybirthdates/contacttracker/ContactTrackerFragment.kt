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


import android.content.Intent

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactTrackerBinding
import com.google.android.material.snackbar.Snackbar

import android.widget.CompoundButton
import androidx.annotation.NonNull
import com.example.android.happybirthdates.contactdetails.ContactDetailsFragmentArgs

/**
 * (c) Fragment with buttons for Contacts, which are saved in DB. Cumulative data are
 * displayed in RecyclerView.
 */
class ContactTrackerFragment : Fragment() {

    /**
     * The (m) is called when (c) ContactTrackerFragment is ready to display content to screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- (c) ContactTrackerFragment <- |fragment layout| fragment_contact_tracker.
        val binding: FragmentContactTrackerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_tracker, container, false)

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- (c) ContactTrackerFragment <- |DB| ContactDatabase.
        val dbPerson = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactDetailsViewModel <- |navigation| (v)s args: (v) isContactDeleted & (v) database  & (v) application.
        val  viewModelFactory = if (arguments != null){
            ContactTrackerViewModelFactory(ContactTrackerFragmentArgs.fromBundle(arguments!!).isContactDeleted, dbPerson, application)
        } else{
            ContactTrackerViewModelFactory(false, dbPerson, application)
        }

        //---------- (c) ContactDetailsViewModel <-  (c) ContactTrackerViewModel.
        val contactTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(ContactTrackerViewModel::class.java)



        //--------------------------- Processing ---------------------------------------------------
        binding.contactTrackerViewModel = contactTrackerViewModel
        binding.lifecycleOwner = this

        //-------------------- 'buttonCreate' <Button>;
        //---------- Observer; Navigating.
        contactTrackerViewModel.navigateToContactCreator.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(ContactTrackerFragmentDirections.actionContactTrackerFragmentToContactCreatorFragment(0))
                // Reset state to make sure we only navigate once, even if the device has a configuration change.
                contactTrackerViewModel.doneNavigatingToContactCreatorFragment()
            }
        })
        //--------------------

        //-------------------- 'alarmToggle' <ToggleButton>;
        //---------- Change listener;
        binding.alarmToggle.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                //--- A. 'alarmToggle' <ToggleButton> is on.
                val toastMsg: String = if (isChecked) {

                    //- (c) ContactStatusService for Push Notifications on.
                    requireActivity().startService(Intent(context, ContactStatusNotificationBackgroundService()::class.java))

                    //- (v) toastMsg -"on"->.
                    getString(R.string.alarm_on_toast)
                }
                //--- B. 'alarmToggle' <ToggleButton> is off.
                else {

                    //- (c) ContactStatusService for Push Notifications off.
                    requireActivity().stopService(Intent(context, ContactStatusNotificationBackgroundService::class.java))

                    //- (v) toastMsg -"off"->.
                    getString(R.string.alarm_off_toast)
                }
                // Show toast to say the alarm is turned on or off.
                Toast.makeText(context!!, toastMsg, Toast.LENGTH_SHORT).show()
            }
        )
        //--------------------

        //-------------------- 'recyclerContactListGrid' <RecyclerView>;
        //---------- SetLayoutManager; (c) GridLayoutManager.
        val manager = GridLayoutManager(activity, 3)
        binding.recyclerViewContactListGrid.layoutManager = manager
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =  when (position) {
                0 -> 3
                else -> 1
            }
        }
        //---------- OnClicked; (c) ContactListListener of (c) ContactListAdapter.
        val adapter = ContactListAdapter(ContactListListener {
                contactId -> contactTrackerViewModel.onContactClicked(contactId)
        })
        //---------- SetAdapter; (c) ContactListAdapter.
        binding.recyclerViewContactListGrid.adapter = adapter
        //---------- Observer; (c) ContactListAdapter <- Watching (v) persons & non empty (v) persons.
        contactTrackerViewModel.persons.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
            }
        })
        //--------------------

        //-------------------- 'Contact' <Image>;
        //---------- Observer; Navigating.
        contactTrackerViewModel.navigateToContactDetails.observe(viewLifecycleOwner, Observer {
            contactId -> contactId?.let {
                this.findNavController().navigate(ContactTrackerFragmentDirections.actionContactTrackerFragmentToContactDetailsFragment(contactId))
                contactTrackerViewModel.doneNavigatingToContactDetailsFragment()
            }
        })
        //--------------------

        //--------------------  'Delete' <Button> of (c) ContactDetailsFragment(!);
        //---------- Observer; Snackbar, Add Observer on state (v) showing Snackbar msg when 'Delete' <Button> of (c) ContactDetailsFragment is pressed.
        contactTrackerViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.cleared_message), Snackbar.LENGTH_SHORT).show()
                // Reset state to make sure Snackbar is only shown once, even if the device has a config change.
                contactTrackerViewModel.doneShowingSnackbar()
            }
        })
        //--------------------

        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }



}
