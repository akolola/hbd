package com.example.android.happybirthdates.contactcreator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.contactdetails.ContactDetailsFragmentArgs
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactCreatorBinding
import com.example.android.happybirthdates.databinding.FragmentImageCropBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_contact_creator.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*







class ContactCreatorFragment : Fragment(), DateSelected {


    companion object {
        private const val TAG = "ContactCreatorFragment"
/*        private const val PERMISSION_CODE = 1001*/

        private val PICK_IMAGE_REQUEST = 1
        private val TAKE_PICTURE_REQUEST = 2
        private val CROP_IMAGE_REQUEST = 3

        private lateinit var binding: FragmentContactCreatorBinding


        private var originalImageUri: Uri? = null


    }


    /**
     * The (m) is called when (c) ContactCreatorFragment is ready to display content to the screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        //--------------------------- Preparation --------------------------------------------------
        //---------- (c) ContactCreatorFragment <- |fragment layout| fragment_contact_creator.
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_creator, container, false)


        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- |navigation| navigation's (v) args.
        val arguments = ContactDetailsFragmentArgs.fromBundle(arguments!!)

        //----------  |DB| ContactDatabase.
        val database = ContactDatabase.getInstance(application).contactDatabaseDao



        //--------------------------- Connection ---------------------------------------------------
        //-------------------- (c) ContactCreatorViewModel;
        //--- <- |navigation| (v)s args: (v) contactPersonKey & (v) database.
        val viewModelFactory = ContactCreatorViewModelFactory(arguments.contactPersonKey, database)
        //--- <- (c) ContactCreatorFragment.
        val contactCreatorViewModel = ViewModelProvider(this, viewModelFactory).get(ContactCreatorViewModel::class.java)
        //--------------------

        //-------------------- |fragment layout| fragment_contact_creator.
        //---------- <- (c) ContactCreatorViewModel.
        binding.contactCreatorViewModel = contactCreatorViewModel
        //---------- <- (c) ContactCreatorFragment.
        // LifecycleOwner should be used for observing changes of LiveData in this binding, i.e. LiveData (v)s in (c) ContactCreatorViewModel.
        // Kotlin syntax like 'binding.setLifecycleOwner(this)'.
        binding.lifecycleOwner = this
        //--------------------


        //--------------------------- Processing ---------------------------------------------------
        //-------------------- 'imageButtonAddPicture' <Image>;
        //---------- Click listener; Contact's image attribute, adding from Android gallery.
        binding.imageButtonAddPicture.setOnClickListener {

            val builder = AlertDialog.Builder(activity)
            builder.setTitle(getString(R.string.chose_option))
                .setItems(arrayOf("Camera", "Gallery")) {  _ ,dialog   ->
                    when (dialog) {
                        0 -> {
                            // Camera option selected, handle accordingly
                            openCamera()
                        }
                        1 -> {
                            // Gallery option selected, handle accordingly
                            // Example: launch gallery intent
                            openGallery()
                        }
                    }
                }
                .setCancelable(true)
                .show()

        }
        //--------------------

        //--------------------  'imageViewContactPicture' <ImageView>;
        //---------- Observer; (v) ldContact, if (v)'s 'value' has 'imageNameId' => -> 'imageURI' param.
/*        contactCreatorViewModel.liveDataContact.observe(viewLifecycleOwner, Observer {
            if(contactCreatorViewModel.liveDataContact.value != null){
                loadImageFromInternalStorage(contactCreatorViewModel.liveDataContact.value!!.imageId.toString())
            }
        })*/
        //--------------------

        //-------------------- 'editTextName' <EditText>;
        // n.a.
        // --------------------

        //-------------------- 'datePickerButton' <Button>;
        //---------- Click listener; Create & display (c) DatePickerFragment.
        binding.buttonDatePicker.setOnClickListener {
            //---------- Show Date Picker
            val datePickerFragment = DatePickerFragment(this)
            datePickerFragment.show(fragmentManager!!, "datePicker")
        }
        //--------------------

        //-------------------- 'buttonSubmit' <Button>;
        //---------- Click listener; (c) CreatorViewModel <- (v)s picture info & name & birthdate.
        binding.buttonSubmit.setOnClickListener {
            binding.apply {
                contactCreatorViewModel.onCreateContact(
                    arguments.contactPersonKey,
                    binding.editTextName.text.toString(),
                    binding.textViewBirthdate.text.toString(),
                    if (binding.imageButtonAddPicture.tag != null) binding.imageButtonAddPicture.tag.toString() else "",
                    byteArrayOf(0b00000001, 0b00000010)
                )
            }
        }
        //---------- Observer; Navigating.
        contactCreatorViewModel.navigateToContactTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    ContactCreatorFragmentDirections.actionContactCreatorFragmentToContactTrackerFragment(false))
                // Reset state to make sure we only navigate once, even if the device has a config change.
                contactCreatorViewModel.doneNavigating()
            }
        })
        //--------------------



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }



    //--------------------------- Image Picker -----------------------------------------------------
/*    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImageGallery()
                } else {
                    Toast.makeText(context!!, "Permission denied", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Pick image permission required")
                }
            }
        }
    }*/

    /**
     * The (m) starts picker to get image for cropping and then use the image in cropping activity.
     */
/*    private fun chooseImageGallery() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(context!!, this)
    }*/

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, imagePickResultIntent: Intent?) {
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
*/

/*    private fun saveImageToInternalStorage(imageBitmap : Bitmap, fileName: String) {
        try {
            // Use compress (m) on (o) Bitmap for: image -> OutputStream
            val fos = context!!.openFileOutput(fileName, Context.MODE_PRIVATE)
            // bitmap -> OutputStream
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e : Exception ) {
            Log.e(TAG, e.toString())
        }
    }*/

    /**
     * For given filename determines path to the images resource &
     * sets 'imageURI' param of 'imageViewContactPicture' <ImageView> to updated val.
     *
     * @param imageFileName to be found in app memory & set as val for imageViewContactPicture' <ImageView>
     */
/*    private fun loadImageFromInternalStorage(imageFileName: String)   {
        try {
            val absolutePath = context!!.getFileStreamPath(imageFileName).absolutePath
            val fin = FileInputStream(absolutePath)
            //--- Update of 'imageViewContactPicture' <ImageView>'s 'URI' param by given image file
            imageButtonAddPicture.setImageURI(Uri.parse(File(absolutePath).toString()))
            fin.close()
        } catch (e : Exception ) {
            Log.e(TAG, e.toString())
        }
    }*/


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.let { intent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)

        if (resultCode == Activity.RESULT_OK && imageData != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    originalImageUri = imageData.data
                    binding.imageButtonAddPicture.setImageURI(originalImageUri)
                    startCropActivity(originalImageUri!!)
                }
                TAKE_PICTURE_REQUEST -> {

                    // Photo was taken successfully, access it using "data" Intent
                    val imageBitmap = imageData?.extras?.get("data") as Bitmap
                    binding.imageButtonAddPicture.setImageBitmap(imageBitmap)

                    // Save the image to a file and get its URI
                    val imageContentUri = getImageContentUri(imageBitmap, requireActivity().contentResolver)

                    imageContentUri?.let { startCropActivity(it) }
                }
                CROP_IMAGE_REQUEST -> {

                    //=== saveImage(bitmap)
                    // Get the image bitmap from the content URI
                    var cropImageUri = imageData.data
                    val imageBitmap = getBitmapFromUri(requireActivity().contentResolver, cropImageUri!!)

                    // Convert the image bitmap to byte array
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream)
                    val imageBytes = byteArrayOutputStream.toByteArray()

                    // Retrieve the image file name from the content URI
                    val fileName = getFileNameFromUri(requireActivity().contentResolver, cropImageUri!!)

                    //ImageCropViewModel
                    binding.apply {

                        //---------------------------------
                        // imageCropViewModel?.onCreateContact(0L, "Jane Doe", "01-01-2000", fileName, imageBytes)!! //<====== ContactDetailsViewModel <- imageCropViewModel
                        binding.imageButtonAddPicture.tag = binding.contactCreatorViewModel?.liveDataContact?.value?.id
                        //---------------------------------


                    }


                }
            }
        }
    }


    // Helper function to get the image bitmap from the content URI
    fun getBitmapFromUri(contentResolver: ContentResolver, imageUri: Uri): Bitmap {
        return BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
    }

    // Helper function to retrieve the image file name from the content URI
    @SuppressLint("Range")
    fun getFileNameFromUri(contentResolver: ContentResolver, imageUri: Uri): String {

        var fileName = ""
        val cursor = contentResolver.query(imageUri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
            }
        }

        return fileName

    }




    private fun startCropActivity(imageUri: Uri) {

        // Create an explicit intent for the crop image action
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(imageUri, "image/*")

        // Set the crop properties
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", 300)
        intent.putExtra("outputY", 300)
        intent.putExtra("scale", true)

        // Set the output format
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())

        // Start the cropping activity
        startActivityForResult(intent, CROP_IMAGE_REQUEST)

    }

    fun getImageContentUri(image: Bitmap, contentResolver: ContentResolver): Uri? {

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                outputStream?.let {
                    image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    return uri
                }
            } catch (e: Exception) {
                contentResolver.delete(uri, null, null)
            }
        }

        return null

    }


    //--------------------------- Date Picker ------------------------------------------------------
    /**
     * (c) DatePickerFragment displaying calendar.
     */
    //---------- (c) inner DatePickerFragment.
    class DatePickerFragment(private val dateSelected: DateSelected): DialogFragment(), DatePickerDialog.OnDateSetListener {

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
        val viewFormattedDate = viewFormatter.format(calendar.time)
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
