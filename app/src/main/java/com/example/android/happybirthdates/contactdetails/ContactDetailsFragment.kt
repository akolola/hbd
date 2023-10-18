package com.example.android.happybirthdates.contactdetails

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactDetailsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_contact_tracker_view_contact_list_grid_item.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


private const val TAG = "ContactDetailsFragment"



/**
 * (c) Fragment with detailed description of one Contact.
 */
class ContactDetailsFragment : Fragment() {


    /**
     * The (m) is called when (c) ContactDetailsFragment is ready to display content to screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        //--------------------------- Declaration --------------------------------------------------
        //---------- (c) ContactDetailsFragment <- |fragment layout| fragment_contact_details.
        val binding: FragmentContactDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_details, container, false)

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- |navigation| navigation's (v) args.
        val arguments = ContactDetailsFragmentArgs.fromBundle(arguments!!)

        //---------- |DB| ContactDatabase.
        val database = ContactDatabase.getInstance(application).contactDatabaseDao



        //--------------------------- Connection ---------------------------------------------------
        //-------------------- (c) ContactDetailsViewModel;
        //---------- <- |navigation| (v)s args: (v) contactPersonKey & (v) database.
        val viewModelFactory = ContactDetailsViewModelFactory(arguments.contactPersonKey, database)
        //---------- <- (c) ContactDetailsFragment.
        val contactDetailsViewModel = ViewModelProvider(this, viewModelFactory).get(ContactDetailsViewModel::class.java)
        //--------------------

        //-------------------- |fragment layout| fragment_contact_details;
        //---------- <- (c) ContactDetailsViewModel.
        binding.contactDetailsViewModel = contactDetailsViewModel
        //---------- <- (c) ContactDetailsFragment.
        // LifecycleOwner should be used for observing changes of LiveData in this binding, i.e. LiveData (v)s in (c) ContactDetailsViewModel.
        // Kotlin syntax like 'binding.setLifecycleOwner(this)'.
        binding.lifecycleOwner = this
        //--------------------


        //--------------------------- Processing ---------------------------------------------------
        //--------------------  'imageViewContactPicture' <ImageView>;
        //---------- Observer; (v) ldContact, if (v)'s 'value' has 'imageNameId' => -> 'imageURI' param.
        contactDetailsViewModel.liveDataContact.observe(viewLifecycleOwner, Observer {

            // Set new img only if it was chosen by user. Otherwise Contact keeps imageNameId "Unnamed" => std 'ic_default_person' img.
            if(contactDetailsViewModel.liveDataContact.value != null && contactDetailsViewModel.liveDataContact.value!!.imageId.toString() != "Unnamed"){
                imageViewContactPicture.setImageURI(createImageUri(it.imageBytes))
            }

        })
        //--------------------

        //-------------------- 'Close' <Button>;
        //---------- Observer;  Navigating.
        contactDetailsViewModel.navigateToContactTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(ContactDetailsFragmentDirections.actionContactDetailsFragmentToContactTrackerFragment(true))
                contactDetailsViewModel.doneNavigatingToContactTrackerFragment()
            }
        })
        //--------------------

        //-------------------- 'Edit' <Button>;
        //---------- Observer; Navigating.
        contactDetailsViewModel.navigateToContactCreator.observe(viewLifecycleOwner, Observer {
                contactId -> contactId?.let {
            this.findNavController().navigate(ContactDetailsFragmentDirections.actionContactDetailsFragmentToContactCreatorFragment(contactId))
            contactDetailsViewModel.doneNavigatingToContactCreatorFragment()
        }
        })
        //--------------------

        //-------------------- 'Delete' <Button>;
        //---------- Observer; Confirmation dialog window & (c) Contact deletion.
        binding.buttonDelete.setOnClickListener {
            var builder = AlertDialog.Builder(activity)
            builder.setTitle(getString(R.string.confirm_delete))
            builder.setMessage(getString(R.string.delete_confirmation_msg))
            builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                contactDetailsViewModel.onDeleteContact(arguments.contactPersonKey)
                dialog.cancel()
            }
            builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            var alert = builder.create()
            alert.show()
        }

        //----------  Observer; Snackbar, Add Observer on state (v) showing Snackbar msg when 'Delete' <Button> is pressed.
        contactDetailsViewModel.showPostDeleteSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.cleared_message), Snackbar.LENGTH_SHORT).show()
                // Reset state to make sure Snackbar is only shown once, even if the device has a config change.
                contactDetailsViewModel.doneShowingPostDeleteSnackbar()
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



}
