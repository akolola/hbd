package com.example.android.happybirthdates.contactcreator

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.android.happybirthdates.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageCropFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PICTURE_REQUEST = 2
    private val CROP_IMAGE_REQUEST = 3

    private lateinit var imageView: ImageView
    private lateinit var cropGalleryButton: Button
    private lateinit var cropCameraButton: Button

    private var originalImageUri: Uri? = null
    private var croppedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        //saveButton.setOnClickListener {
        ///saveImageToStorage()
        // }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, TAKE_PICTURE_REQUEST)
    }

    private fun startCropActivity(uri: Uri) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", 300)
        intent.putExtra("outputY", 300)
        intent.putExtra("return-data", false)
        croppedImageUri = getOutputMediaFileUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, croppedImageUri)
        startActivityForResult(intent, CROP_IMAGE_REQUEST)
    }

    private fun saveImageToStorage() {
        val originalImagePath = originalImageUri?.path
        val croppedImagePath = croppedImageUri?.path

        if (originalImagePath != null && croppedImagePath != null) {
            val originalBitmap = BitmapFactory.decodeFile(originalImagePath)
            val croppedBitmap = BitmapFactory.decodeFile(croppedImagePath)

            // Save originalBitmap and croppedBitmap to internal storage

            // Save the images using your desired implementation, e.g., store it in internal storage using FileOutputStream or in a local database

            // After saving, you can show a success message to the user
        }
    }

    private fun getOutputMediaFileUri(): Uri {
        val mediaStorageDir = File(activity?.getExternalFilesDir(null), "CroppedImages")
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }

        //val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile = File(mediaStorageDir.path + File.separator + "IMG_Croped" + ".jpg")
        mediaFile.createNewFile()
        return FileProvider.getUriForFile(requireContext(), "com.example.android.happybirthdates.fileprovider", mediaFile)


/*        val imagePath: File = File(requireContext().filesDir, "my_images")
        val newFile = File(imagePath, "default_image.jpg")
        val contentUri: Uri = FileProvider.getUriForFile(requireContext(), "com.example.android.happybirthdates.fileprovider", newFile)
        return contentUri*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    originalImageUri = data.data
                    imageView.setImageURI(originalImageUri)
                    startCropActivity(originalImageUri!!)
                }
                TAKE_PICTURE_REQUEST -> {
                    val image = data.extras?.get("data") as Bitmap
                    ///originalImageUri = saveImageToGallery(image) To be implemented
                    imageView.setImageBitmap(image)
                    startCropActivity(originalImageUri!!)
                }
                CROP_IMAGE_REQUEST -> {
                    imageView.setImageURI(croppedImageUri)
                }
            }
        }
    }
}