package com.example.android.happybirthdates.contactcreator

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.contactdetails.ContactDetailsFragmentArgs
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentImageCropBinding
import java.io.ByteArrayOutputStream


class ImageCropFragment : Fragment() {



    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PICTURE_REQUEST = 2
    private val CROP_IMAGE_REQUEST = 3

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
        binding.cropGalleryButton.setOnClickListener {

            //--- Step 1. Pic
            openGallery()
        }

        binding.cropCameraButton.setOnClickListener {
            openCamera()
        }

        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }


    private fun  openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.let { intent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
        }
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
                        imageCropViewModel?.onCreateContact(0L, "Jane Doe", "01-01-2000", fileName, imageBytes)!!
                        binding.imageView.tag = binding.imageCropViewModel?.liveDataContact?.value?.id
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

}
