package com.example.android.happybirthdates.contactcloud

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.happybirthdates.R

class ContactCloudFragment : Fragment() {

    companion object {
        fun newInstance() = ContactCloudFragment()
    }

    private lateinit var viewModel: ContactCloudViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact_cloud, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactCloudViewModel::class.java)
        // TODO: Use the ViewModel
    }

}