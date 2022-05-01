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


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent

import android.os.Build
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
import android.graphics.Color
import androidx.core.content.ContextCompat.getSystemService

import android.widget.CompoundButton

/**
 * (c) Fragment with buttons for Contacts, which are saved in DB. Cumulative data are
 * displayed in RecyclerView.
 */
class ContactTrackerFragment : Fragment() {

    //---------- (v) for Push Notifications.
    private val PRIMARY_CHANNEL_ID = "primary_notification_channel"
    private var mNotificationManager: NotificationManager? = null

    /**
     * The (m) is called when (c) ContactTrackerFragment is ready to display content to the screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- |fragment layout| fragment_contact_tracker -> (c) ContactTrackerFragment.
        val binding: FragmentContactTrackerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_tracker, container, false)

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- |DB| ContactDatabase -> (c) ContactTrackerFragment.
        val dbPerson = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactTrackerViewModel -> (c) ContactTrackerFragment.
        val viewModelFactory = ContactTrackerViewModelFactory(dbPerson, application)
        val contactTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(ContactTrackerViewModel::class.java)

        //--------------------------- Processing ---------------------------------------------------
        binding.contactTrackerViewModel = contactTrackerViewModel
        binding.lifecycleOwner = this

        //-------------------- <Button> 'buttonCreate'.
        //---------- Observer; Navigating.
        contactTrackerViewModel.navigateToContactCreator.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(ContactTrackerFragmentDirections.actionContactTrackerFragmentToContactCreatorFragment())
                // Reset state to make sure we only navigate once, even if the device has a configuration change.
                contactTrackerViewModel.doneNavigatingToContactCreatorFragment()
            }
        })

        //-------------------- <RecyclerView> 'recyclerContactListGrid'.
        //----------  (c) GridLayoutManager -> (c) ContactListAdapter.
        val manager = GridLayoutManager(activity, 3)
        binding.recyclerViewContactListGrid.layoutManager = manager
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =  when (position) {
                0 -> 3
                else -> 1
            }
        }

        //---------- (c) ContactListAdapter -> (c) ContactTrackerFragment.
        val adapter = ContactListAdapter(ContactListListener { contactId -> contactTrackerViewModel.onContactClicked(contactId) })
        binding.recyclerViewContactListGrid.adapter = adapter

        //---------- Observer; Watch (v) persons & non empty (v) persons --> (c) ContactListAdapter.
        contactTrackerViewModel.persons.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
            }
        })

        //---------- Observer; Navigating.
        contactTrackerViewModel.navigateToContactDetails.observe(viewLifecycleOwner, Observer {
            contactId -> contactId?.let {
                this.findNavController().navigate(ContactTrackerFragmentDirections.actionContactTrackerFragmentToContactDetailsFragment(contactId))
                contactTrackerViewModel.doneNavigatingToContactDetailsFragment()
            }
        })

        //-------------------- <Button> Clear.
        //---------- Observer; Snackbar.
        // Add an Observer on the state var showing a Snackbar msg when <Button> Clear is pressed.
        contactTrackerViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_SHORT // How long to display the msg.
                ).show()
                // Reset state to make sure Snackbar is only shown once, even if the device has a config change.
                contactTrackerViewModel.doneShowingSnackbar()
            }
        })

        //-------------------- <ToggleButton> 'alarmToggle' | (v)s for Push Notifications.
        binding.alarmToggle.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                //--- A. <ToggleButton> 'alarmToggle' is turned on.
                val toastMsg: String = if (isChecked) {

                    //- (c) ContactStatusService
                    requireActivity().startService(Intent(context, ContactStatusBackgroundService()::class.java))

                    //- (v) toastMsg -"on"->.
                    getString(R.string.alarm_on_toast)
                }
                //--- B. <ToggleButton> 'alarmToggle' is turned off.
                else {

                    //- (c) NotificationManager.
                    mNotificationManager!!.cancelAll()

                    //- (c) ContactStatusService
                    requireActivity().stopService(Intent(context, ContactStatusBackgroundService::class.java))

                    //- (v) toastMsg -"off->.
                    getString(R.string.alarm_off_toast)
                }
                // Show toast to say the alarm is turned on or off.
                Toast.makeText(context!!, toastMsg, Toast.LENGTH_SHORT).show()
            }
        )
        createNotificationChannel()


        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }


    //--------------------------- Notification -----------------------------------------------------
    /**
     *   Create (c) NotificationChannel if >= Android ver OREO.
     */
    private fun createNotificationChannel() {

        //- (c) NotificationManager.
        mNotificationManager = getSystemService(context!!, NotificationManager::class.java)

        // (c) NotificationManager -(c) NotificationChannel->.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create (c) NotificationChannel. Add params.
            val notificationChannel = NotificationChannel(PRIMARY_CHANNEL_ID,"Stand up notification", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Notifies every 15 minutes to stand up and walk"
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }

    }


}
