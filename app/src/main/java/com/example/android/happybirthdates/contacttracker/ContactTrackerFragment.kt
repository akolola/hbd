/*
 * Copyright 2021, The Android Open Source Project
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
import android.app.PendingIntent
import android.content.Intent

import android.os.Build
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.core.content.ContextCompat
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
import android.os.SystemClock
import androidx.core.content.ContextCompat.getSystemService

import android.app.AlarmManager

import android.widget.CompoundButton
import android.widget.ToggleButton
import com.example.android.happybirthdates.contacttracker.AlarmReceiver

/**
 * (c) Fragment with buttons for Contacts, which are saved in DB. Cumulative data are
 * displayed in RecyclerView.
 */
class ContactTrackerFragment : Fragment() {

    // Notification ID.
    private val NOTIFICATION_ID = 0

    // Notification channel ID.
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

        //---------- (c) NotificationManager & (c) AlarmManager
        mNotificationManager = getSystemService(context!!, NotificationManager::class.java)
        var alarmManager = getSystemService(context!!, AlarmManager::class.java)
        // Set up the Notification Broadcast Intent.
        val notifyIntent = Intent(context, AlarmReceiver::class.java)
        val notifyPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)



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
                    Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                // Reset state to make sure the snackbar is only shown once, even if the device
                // has a configuration change.
                contactTrackerViewModel.doneShowingSnackbar()
            }
        })

        //---------- Notification
        // Set the click listener for the toggle button.
        binding.alarmToggle.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                val toastMessage: String = if (isChecked) {

                    val repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES
                    val triggerTime = (SystemClock.elapsedRealtime()) //+ repeatInterval)

                    // If the Toggle is turned on, set the repeating alarm with a 15 minute interval.
                    alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, notifyPendingIntent)

                    // Set the toast message for the "on" case.
                    getString(R.string.alarm_on_toast)
                } else {
                    // Cancel notification if the alarm is turned off.
                    mNotificationManager!!.cancelAll()
                    alarmManager?.cancel(notifyPendingIntent)
                    // Set the toast message for the "off" case.
                    getString(R.string.alarm_off_toast)
                }

                // Show a toast to say the alarm is turned on or off.
                Toast.makeText(context!!, toastMessage, Toast.LENGTH_SHORT).show()
            }
        )


        createNotificationChannel()


        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }


    //->//--------------------------- Notification -----------------------------------------------------
    //Creates a Notification channel, for OREO and higher.
    open fun createNotificationChannel() {

        // Create a notification manager object.
        mNotificationManager = getSystemService(context!!, NotificationManager::class.java)

        // Notification channels are only available in OREO and higher. So, add a check on SDK version.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel with all the parameters.
            val notificationChannel = NotificationChannel(PRIMARY_CHANNEL_ID,"Stand up notification", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Notifies every 15 minutes to stand up and walk"
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }

    }


}
