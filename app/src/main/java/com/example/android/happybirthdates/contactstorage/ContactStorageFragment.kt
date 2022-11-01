package com.example.android.happybirthdates.contactstorage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.contactcloud.ContactCloudFragment

class ContactStorageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_cloud, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactBackupFragment = ContactCloudFragment ()
        childFragmentManager.beginTransaction().apply {
            add(R.id.contact_cloud_fragment, contactBackupFragment)
            commit()
        }

    }


}