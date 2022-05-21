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

package com.example.android.happybirthdates.contactdetails

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_contact_tracker_view_contact_list_grid_item.*
import java.io.File
import java.io.FileInputStream

private const val TAG = "ContactDetailsFragment"

/**
 * (c) Fragment with one Contact detailed description.
 */
class ContactDetailsFragment : Fragment() {

    /**
     * The (m) is called when (c) ContactDetailsFragment is ready to display content to the screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- (c) ContactDetailsFragment <- <xml> |fragment layout| fragment_contact_details
        val binding: FragmentContactDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_details, container, false)

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- |navigation| navigation's (v) args.
        val arguments = ContactDetailsFragmentArgs.fromBundle(arguments!!)

        //---------- |DB| ContactDatabase.
        val database = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactDetailsViewModel <- |navigation| (v)s arguments: (v) contactPersonKey & (v) database.
        val viewModelFactory = ContactDetailsViewModelFactory(arguments.contactPersonKey, database)
        val contactDetailsViewModel = ViewModelProvider(this, viewModelFactory).get(ContactDetailsViewModel::class.java)



        //--------------------------- Processing ---------------------------------------------------
        binding.contactDetailsViewModel = contactDetailsViewModel
        binding.lifecycleOwner = this       // Kotlin syntax like 'binding.setLifecycleOwner(this)'

        //---------- Observer;  (v) ldPerson; Value emptiness.
        contactDetailsViewModel.ldPerson.observe(viewLifecycleOwner, Observer {
            if(contactDetailsViewModel.ldPerson.value != null){
                loadImageFromInternalStorage(contactDetailsViewModel.ldPerson.value!!.imageNameId.toString())
            }
        })


        //---------- Observer;  <Button> 'Close'; Navigating.
        contactDetailsViewModel.navigateToContactTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(ContactDetailsFragmentDirections.actionContactDetailsFragmentToContactTrackerFragment())
                contactDetailsViewModel.doneNavigatingToContactTrackerFragment()
            }
        })



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }


    /**
     * For given filename determines path to the images resource &
     * sets 'URI' param of 'imageViewContactPicture' <ImageView> to updated val.
     *
     * @param imageFileName to be found in app memory & set as val for imageViewContactPicture' <ImageView>
     */
    private fun loadImageFromInternalStorage(imageFileName: String) {
        try {
            val absolutePath = context!!.getFileStreamPath(imageFileName).absolutePath
            val fin = FileInputStream(absolutePath)
            ///val bitmap = BitmapFactory.decodeStream(fin)
            //--- Update of 'imageViewContactPicture' <ImageView>'s 'URI' param by given image file
            imageViewContactPicture.setImageURI(Uri.parse(File(absolutePath).toString()))
            fin.close()
        } catch (e : Exception ) {
            Log.e(TAG, e.toString())
        }
    }


}
