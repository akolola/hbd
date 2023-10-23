package com.example.android.happybirthdates.contactcreator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.contactdetails.ContactDetailsFragmentArgs
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactCreatorBinding
import kotlinx.android.synthetic.main.fragment_contact_creator.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*






class ContactCreatorFragment : Fragment(), DateSelected {


    companion object {

        private const val TAG = "ContactCreatorFragment"

        private lateinit var binding: FragmentContactCreatorBinding

        private val GALLERY_IMAGE_REQUEST = 1
        private val CAMERA_IMAGE_REQUEST = 2
        private val CROP_IMAGE_REQUEST = 3

        private var preSavedImageUri: Uri? = null
        private var originalImageUri: Uri? = null
        private var cropImageUri: Uri? = null


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
                            startCameraActivity()
                        }
                        1 -> {
                            startGalleryActivity()
                        }
                    }
                }
                .setCancelable(true)
                .show()

        }
        //--------------------

        //--------------------  'imageViewContactPicture' <ImageView>;
        //---------- Observer; (v) liveDataContact, if (v)'s 'value' has 'imageNameId' => -> 'imageURI' param.
        contactCreatorViewModel.liveDataContact.observe(viewLifecycleOwner, Observer {
            if(contactCreatorViewModel.liveDataContact.value != null){

                // Set new img only if it was chosen by user. Otherwise Contact keeps imageNameId "Unnamed" => std 'ic_default_person' img.
                if(contactCreatorViewModel.liveDataContact.value != null && contactCreatorViewModel.liveDataContact.value!!.imageId.toString() != "Unnamed"){
                    preSavedImageUri = createImageUri(it.imageBytes)
                    binding.imageButtonAddPicture.setImageURI(preSavedImageUri)     //  loadImageFromInternalStorage(contactCreatorViewModel.liveDataContact.value!!.imageId.toString())
                }

            }

        })

        //--------------------


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


                // (v) fileName of image from content URI <- (v) content URI
                val fileName = getFileNameFromUri(requireActivity().contentResolver, if(cropImageUri!! != null && preSavedImageUri!! != null)  cropImageUri!! else preSavedImageUri!!)

                // (v) imageBytes of image <- (v) content URI
                val imageBitmap = getBitmapFromUri(requireActivity().contentResolver, if(cropImageUri!! != null && preSavedImageUri!! != null)  cropImageUri!! else preSavedImageUri!!)
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                // (c) Contact -> |DB|
                contactCreatorViewModel.onCreateContact(arguments.contactPersonKey, binding.editTextName.text.toString(), binding.textViewBirthdate.text.toString(), fileName, imageBytes)

            }
        }
        //---------- Observer; Navigating.
        contactCreatorViewModel.navigateToContactTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(ContactCreatorFragmentDirections.actionContactCreatorFragmentToContactTrackerFragment(false))
                // Reset state to make sure we only navigate once, even if the device has a config change.
                contactCreatorViewModel.doneNavigating()
            }
        })
        //--------------------



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }


    /**
     * For given image from DB sets 'imageUri' param
     *
     * @param imageBytes representing an image to be used to compose an image file and then - its URI
     */
    fun createImageUri(imageBytes: ByteArray): Uri? {
        var imageUri: Uri? = null
        try {
            val cacheDir = context!!.cacheDir
            val imageFile = File.createTempFile("image", ".jpg", cacheDir)
            val fos = FileOutputStream(imageFile)
            fos.write(imageBytes)
            fos.flush()
            fos.close()
            imageUri = Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageUri
    }




    //--------------------------- Image Picker -----------------------------------------------------
    private fun startCameraActivity() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.let { intent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    private fun startGalleryActivity() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST)
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, imageIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageIntent)

        if (resultCode == Activity.RESULT_OK && imageIntent != null) {
            when (requestCode) {
                // Gallery activity result
                GALLERY_IMAGE_REQUEST -> {
                    // (c) Uri
                    originalImageUri = imageIntent.data
                    // Crop Image Activity Start. (v) imageContentUri -> (c') CropActivity
                    startCropActivity(originalImageUri!!)
                }
                // Camera activity result
                CAMERA_IMAGE_REQUEST -> {
                    //  (c) Bitmap
                    val originalImageBitmap = imageIntent?.extras?.get("data") as Bitmap
                    // (c) Uri
                    val orignialImageContentUri = getImageContentUri(originalImageBitmap, requireActivity().contentResolver)
                    // Crop Image Activity Start. (v) imageContentUri -> (c') CropActivity
                    orignialImageContentUri?.let { startCropActivity(it) }
                }
                // Crop activity result
                CROP_IMAGE_REQUEST -> {
                    // (c) Uri
                    cropImageUri = imageIntent.data
                    // 'imageButtonAddPicture' <imageButton>;
                    binding.apply {
                        binding.imageButtonAddPicture.tag = binding.contactCreatorViewModel?.liveDataContact?.value?.id
                        binding.imageButtonAddPicture.setImageURI(cropImageUri)
                    }
                }
            }
        }

    }



    private fun getBitmapFromUri(contentResolver: ContentResolver, imageUri: Uri): Bitmap {
        return BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
    }


    @SuppressLint("Range")
    private fun getFileNameFromUri(contentResolver: ContentResolver, imageUri: Uri): String {

        var fileName = ""
        val cursor = contentResolver.query(imageUri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
            }
        }

        return fileName

    }

    private fun getImageContentUri(image: Bitmap, contentResolver: ContentResolver): Uri? {

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                outputStream?.let {
                    image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
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
