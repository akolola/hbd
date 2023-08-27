package com.example.android.happybirthdates.contactcreator

import android.app.Activity
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
import androidx.fragment.app.Fragment
import com.example.android.happybirthdates.R


class ImageCropFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PICTURE_REQUEST = 2
    private val CROP_IMAGE_REQUEST = 3

    private lateinit var imageView: ImageView
    private lateinit var cropGalleryButton: Button
    private lateinit var cropCameraButton: Button

    private var originalImageUri: Uri? = null
    private var croppedImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_image_crop, container, false)
        imageView = view.findViewById(R.id.image_view)
        cropGalleryButton = view.findViewById(R.id.crop_gallery_button)
        cropCameraButton = view.findViewById(R.id.crop_camera_button)

        cropGalleryButton.setOnClickListener {
            openGallery()
        }

        cropCameraButton.setOnClickListener {
            openCamera()
        }

        return view
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)

        if (resultCode == Activity.RESULT_OK && imageData != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    originalImageUri = imageData.data
                    imageView.setImageURI(originalImageUri)
                    startCropActivity(originalImageUri!!)
                }
                TAKE_PICTURE_REQUEST -> {

                    // Photo was taken successfully, access it using "data" Intent
                    val imageBitmap = imageData?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(imageBitmap)

                    ///originalImageUri = data.data  // <- ERROR. originalImageUri=null
                    ///startCropActivity(originalImageUri!!)
                }
                CROP_IMAGE_REQUEST -> {
                    imageView.setImageURI(imageData.data)
                }
            }
        }
    }
}