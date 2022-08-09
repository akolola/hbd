package com.example.android.happybirthdates.contactbackup

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.happybirthdates.R

class ContactBackupFragment : Fragment() {

    companion object {
        fun newInstance() = ContactBackupFragment()
    }

    private lateinit var viewModel: ContactBackupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_backup, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactBackupViewModel::class.java)
        // TODO: Use the ViewModel
    }

}