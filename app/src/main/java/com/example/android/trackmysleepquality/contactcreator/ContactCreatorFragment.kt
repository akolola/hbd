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

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.ContactDatabase
import com.example.android.trackmysleepquality.databinding.FragmentContactCreatorBinding
import kotlinx.android.synthetic.main.fragment_contact_creator.*
import java.text.SimpleDateFormat
import java.util.*


class ContactCreatorFragment : Fragment(), DateSelected {



    /**
     * Called when the Fragment is ready to display content to the screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- <xml> |fragment| fragment_contact_creator
        var binding: FragmentContactCreatorBinding = DataBindingUtil.inflate(
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

        //---------- Click listener; <EditText> 'Name Edit' & <Button> 'Submit'.
        binding.buttonSubmit.setOnClickListener {
            binding.apply {
                contactCreatorViewModel.onCreateContact(binding.editTextName.text.toString())
            }
        }

        //---------- Click listener; <Button> 'datePickerButton'.
        binding.buttonDatePicker.setOnClickListener {
            //---------- Show Date Picker
            val datePickerFragment = DatePickerFragment(this)
            datePickerFragment.show(fragmentManager!!, "datePicker")
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


    //--------------------------- DatePicker -------------------------------------------------------
    /**
     * (c) DatePicker Fragment displaying the calendar
     */
    //---------- (c) inner DatePickerFragment
    class DatePickerFragment(val dateSelected: DateSelected): DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month  = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(context!!, this, year,month,dayOfMonth)
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            dateSelected.receiveDate(year, month, dayOfMonth)
            Log.d(ContentValues.TAG, "Got the date")
        }

    }

    /**
     * The method is triggered by a user after date picking
     */
    //---------- (m) inner DatePickerFragment
    override fun receiveDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = GregorianCalendar()
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)

        val viewFormatter = SimpleDateFormat("dd-MMM-yyyy")
        var viewFormattedDate = viewFormatter.format(calendar.getTime())
        buttonDatePicker.text = viewFormattedDate
    }




}




//--------------------------- (i)  DateSelected ----------------------------------------------------
/**
 * (i) DateSelected to be implemented to display date
 */
interface DateSelected{
     fun  receiveDate(year: Int, month: Int, dayOfMonth: Int)
}
