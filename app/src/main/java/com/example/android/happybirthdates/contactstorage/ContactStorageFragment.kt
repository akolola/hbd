package com.example.android.happybirthdates.contactstorage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.contactbackup.ContactBackupFragment
import com.example.android.happybirthdates.contactcloud.ContactCloudFragment



private const val TAG = "ContactStorageFragment"



class ContactStorageFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_storage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val contactBackupFragment = ContactBackupFragment ()
        childFragmentManager.beginTransaction().apply {
            add(R.id.contact_backup_fragment, contactBackupFragment)
            commit()
        }

        val contactCloudFragment = ContactCloudFragment ()
        childFragmentManager.beginTransaction().apply {
            add(R.id.contact_cloud_fragment, contactCloudFragment)
            commit()
        }


    }


}