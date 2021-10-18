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

package com.example.android.happybirthdates.contactcreator

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactCreatorBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_contact_creator.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "ContactCreatorFragment"


class ContactCreatorFragment : Fragment(), DateSelected {



    /**
     * The (m) is called when (c) ContactCreatorFragment is ready to display content to the screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- |fragment layout| fragment_contact_creator
        var binding: FragmentContactCreatorBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_creator, container, false)

        //---------- Technical (v) application
        val application = requireNotNull(this.activity).application

        //----------  |DB| Contact
        val dataSource = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactCreatorViewModel
        val viewModelFactory = ContactCreatorViewModelFactory(dataSource)
        val contactCreatorViewModel = ViewModelProvider(this, viewModelFactory).get(ContactCreatorViewModel::class.java)



        //--------------------------- Processing ---------------------------------------------------
        //---------- (c) ContactCreatorViewModel -> (c) ContactTrackerFragment.
        binding.contactCreatorViewModel = contactCreatorViewModel


        //---------- Click listener; <Image> 'imageButtonAddPicture'.
        binding.imageButtonAddPicture.setOnClickListener {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                   if (checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, PERMISSION_CODE)
                   } else{
                       // start picker to get image for cropping and then use the image in cropping activity
                       CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(context!!, this)
                   }
              }else{
                  CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(context!!, this)
              }
        }

        //---------- Click listener; <EditText> 'editTextName' & <Button> 'buttonSubmit'.
        binding.buttonSubmit.setOnClickListener {
            binding.apply {
                contactCreatorViewModel.onCreateContact(
                    binding.editTextName.text.toString(),
                    binding.textViewBirthdate.text.toString(),
                    binding.imageButtonAddPicture.tag.toString())
            }
        }

        //---------- Click listener; <Button> 'datePickerButton'. Create & display (c)DatePickerFragment.
        binding.buttonDatePicker.setOnClickListener {
            //---------- Show Date Picker
            val datePickerFragment = DatePickerFragment(this)
            datePickerFragment.show(fragmentManager!!, "datePicker")
        }

        //---------- Observer; <Button> 'buttonSubmit'; Navigating.
        contactCreatorViewModel.navigateToContactTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    ContactCreatorFragmentDirections.actionContactCreatorFragmentToContactTrackerFragment())
                // Reset state to make sure we only navigate once, even if the device has a config change.
                contactCreatorViewModel.doneNavigating()
            }
        })



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }

    //--------------------------- Image Picker -------------------------------------------------------
    companion object {
        private const val PERMISSION_CODE = 1001
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(context!!, this)
                } else {
                    Toast.makeText(context!!, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imagePickResultIntent: Intent?) {
        if(resultCode == Activity.RESULT_OK && imagePickResultIntent!=null){

            // handle result of CropImageActivity
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                val result = CropImage.getActivityResult(imagePickResultIntent)

                if (resultCode == Activity.RESULT_OK){
                    val resultImageUri: Uri? = result.uri

                    val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, resultImageUri)
                    imageButtonAddPicture.tag = "${UUID.randomUUID()}.png"
                    val fileName = imageButtonAddPicture.tag.toString()
                    saveImageToInternalStorage(bitmap, fileName)
                    loadImageFromInternalStorage(fileName)

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, result.error.toString())
                }

            }
        }
    }

    //--------------------------- File -------------------------------------------------------
    private fun saveImageToInternalStorage(imageBitmap : Bitmap, fileName: String) {
        try {
            // Use compress (m) on (o) Bitmap for: image -> OutputStream
            val fos = context!!.openFileOutput(fileName, Context.MODE_PRIVATE)
            // bitmap -> OutputStream
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e : Exception ) {
            Log.e(TAG, e.toString())
        }
    }

    private fun loadImageFromInternalStorage(fileName: String) {
        try {
            val absolutePath = context!!.getFileStreamPath(fileName).absolutePath
            val fin = FileInputStream(absolutePath)
            ///val bitmap = BitmapFactory.decodeStream(fin)
            imageButtonAddPicture.setImageURI(Uri.parse(File(absolutePath).toString()))
            fin.close()
        } catch (e : Exception ) {
            Log.e(TAG, e.toString())
        }
    }



    //--------------------------- DatePicker -------------------------------------------------------
    /**
     * (c) DatePickerFragment displaying calendar.
     */
    //---------- (c) inner DatePickerFragment.
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
        }

    }

    /**
     * The (m) is triggered by a user after date picking.
     */
    //---------- (m) inner DatePickerFragment.
    override fun receiveDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = GregorianCalendar()
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)

        val viewFormatter = SimpleDateFormat("dd.MM.yyyy")
        var viewFormattedDate = viewFormatter.format(calendar.getTime())
        textViewBirthdate.text = viewFormattedDate
    }


}




//--------------------------- (i)  DateSelected ----------------------------------------------------
/**
 * (i) DateSelected to be implemented to display date.
 */
interface DateSelected{
     fun  receiveDate(year: Int, month: Int, dayOfMonth: Int)
}
