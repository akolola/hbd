package com.example.android.happybirthdates.contactcreator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.happybirthdates.database.ContactDatabaseDao

class ImageCropViewModelFactory(private val contactKey: Long, private val dataSource: ContactDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageCropViewModel::class.java)) {
            return ImageCropViewModel(contactKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}