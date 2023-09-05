package com.example.android.happybirthdates.contactcreator

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.contactdetails.ContactDetailsFragmentArgs
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactCreatorBinding
import com.example.android.happybirthdates.databinding.FragmentImageCropBinding
import java.io.ByteArrayOutputStream


class ImageCropFragment : Fragment() {



    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PICTURE_REQUEST = 2
    private val CROP_IMAGE_REQUEST = 3

   // private lateinit var imageView: ImageView
   //private lateinit var cropGalleryButton: Button
    //private lateinit var cropCameraButton: Button

    private lateinit var binding: FragmentImageCropBinding

    private var originalImageUri: Uri? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- (c) ContactCreatorFragment <- |fragment layout| fragment_contact_creator.
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_image_crop, container, false)

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- |navigation| navigation's (v) args.
        val arguments = ContactDetailsFragmentArgs.fromBundle(arguments!!)

        //----------  |DB| ContactDatabase.
        val database = ContactDatabase.getInstance(application).contactDatabaseDao



        //--------------------------- Connection ---------------------------------------------------
        //-------------------- (c) ImageCropViewModel;
        //--- <- |navigation| (v)s args: (v) contactPersonKey & (v) database.
        val viewModelFactory = ImageCropViewModelFactory(arguments.contactPersonKey, database)
        //--- <- (c) ImageCropFragment.
        val imageCropViewModel = ViewModelProvider(this, viewModelFactory).get(ImageCropViewModel::class.java)
        //--------------------

        //-------------------- |fragment layout| fragment_contact_creator.
        //---------- <- (c) ImageCropViewModel.
        binding.imageCropViewModel = imageCropViewModel
        //---------- <- (c) ImageCropFragment.
        // LifecycleOwner should be used for observing changes of LiveData in this binding, i.e. LiveData (v)s in (c) ContactCreatorViewModel.
        // Kotlin syntax like 'binding.setLifecycleOwner(this)'.
        binding.lifecycleOwner = this
        //--------------------


        //--------------------------- Processing ---------------------------------------------------
        //val view = inflater.inflate(R.layout.fragment_image_crop, container, false)
        //imageView = view.findViewById(R.id.image_view)//imageView =  binding.imageView
        //cropGalleryButton = view.findViewById(R.id.crop_gallery_button)
        //cropCameraButton = view.findViewById(R.id.crop_camera_button)


        binding.cropGalleryButton.setOnClickListener {

            //--- Step 1. Pic
            openGallery()

            //--- Step 2. Create Con
            binding.apply {
                imageCropViewModel.onCreateContact(
                    arguments.contactPersonKey,
                    "John Doe",
                    "01-01-2000",
                    if (binding.imageView.tag != null) binding.imageView.tag.toString() else "",
                    byteArrayOf(0b00000001, 0b00000010)
                )
            }
        }

        binding.cropCameraButton.setOnClickListener {
            openCamera()
        }

        //--------------------------- Finish -------------------------------------------------------
        return binding.root // return view
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.let { intent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
        }
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




    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)

        if (resultCode == Activity.RESULT_OK && imageData != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    originalImageUri = imageData.data
                    binding.imageView.setImageURI(originalImageUri)
                    startCropActivity(originalImageUri!!)
                }
                TAKE_PICTURE_REQUEST -> {

                    // Photo was taken successfully, access it using "data" Intent
                    val imageBitmap = imageData?.extras?.get("data") as Bitmap
                    binding.imageView.setImageBitmap(imageBitmap)

                    // Save the image to a file and get its URI
                    val imageContentUri = getImageContentUri(imageBitmap, requireActivity().contentResolver)  //saveImageToFile(imageBitmap)

                    imageContentUri?.let { startCropActivity(it) }
                }
                CROP_IMAGE_REQUEST -> {

                    ///saveImage(bitmap)
                    ///loadImage()

                    binding.imageView.setImageURI(imageData.data) //
                }
            }
        }
    }


/*    // Save the image in local SQLite database
    fun saveImageToDatabase(context: Context, imageUri: Uri) {

        // Get the image bitmap from the content URI
        val bitmap = getBitmapFromUri(context.contentResolver, imageUri)


        // Convert the image bitmap to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()


        // Retrieve the image file name from the content URI
        val fileName = getFileNameFromUri(context.contentResolver, imageUri)

        // Save the image in the local SQLite database
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseContract.ImageEntry.COLUMN_NAME_FILENAME, fileName)
            put(DatabaseContract.ImageEntry.COLUMN_NAME_IMAGE, imageBytes)
        }

        db.insert(DatabaseContract.ImageEntry.TABLE_NAME, null, values)

        // Close the database connection
        dbHelper.close()

    }
    // Helper function to get the image bitmap from the content URI
    fun getBitmapFromUri(contentResolver: ContentResolver, imageUri: Uri): Bitmap {

        return BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

    }
    // Helper function to retrieve the image file name from the content URI
    fun getFileNameFromUri(contentResolver: ContentResolver, imageUri: Uri): String {

        var fileName = ""
        val cursor = contentResolver.query(imageUri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
            }
        }

        return fileName

    }*/



}
