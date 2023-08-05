package com.example.android.happybirthdates.contacttracker

import android.content.Context
import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactTrackerBinding
import kotlinx.android.synthetic.main.fragment_contact_tracker.*


private const val TAG = "ContactTrackerFragment"



/**
 * (c) Fragment with buttons for Contacts, which are saved in DB. Cumulative data are
 * displayed in RecyclerView.
 */
class ContactTrackerFragment : Fragment() {



    /**
     * The (m) is called when (c) ContactTrackerFragment is ready to display content to screen.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        //--------------------------- Preparation --------------------------------------------------
        //---------- (c) ContactTrackerFragment <- |fragment layout| fragment_contact_tracker.
        val binding: FragmentContactTrackerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_tracker, container, false)

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- (c) ContactTrackerFragment <- |DB| ContactDatabase.
        val dbPerson = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactDetailsViewModel <- |navigation| (v)s args: (v) isContactDeleted & (v) database  & (v) application.
        val  viewModelFactory = if (arguments != null){
            ContactTrackerViewModelFactory(ContactTrackerFragmentArgs.fromBundle(requireArguments()).isContactDeleted, dbPerson, application)
        } else{
            ContactTrackerViewModelFactory(false, dbPerson, application)
        }

        //---------- (c) ContactDetailsViewModel <-  (c) ContactTrackerViewModel.
        val contactTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(ContactTrackerViewModel::class.java)



        //--------------------------- Processing ---------------------------------------------------
        binding.contactTrackerViewModel = contactTrackerViewModel
        binding.lifecycleOwner = this

        //-------------------- 'buttonCreate' <Button>;
        //---------- Observer; Navigating.
        contactTrackerViewModel.navigateToContactCreator.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(ContactTrackerFragmentDirections.actionContactTrackerFragmentToContactCreatorFragment(0))
                // Reset state to make sure we only navigate once, even if the device has a configuration change.
                contactTrackerViewModel.doneNavigatingToContactCreatorFragment()
            }
        })
        //--------------------

        //-------------------- 'buttonBackup' <Button>;
        //---------- Observer; Navigating.
        contactTrackerViewModel.navigateToContactStorage.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(ContactTrackerFragmentDirections.actionContactTrackerFragmentToContactStorageFragment())
                // Reset state to make sure we only navigate once, even if the device has a configuration change.
                contactTrackerViewModel.doneNavigatingToContactStorageFragment()
            }
        })
        //--------------------

        //-------------------- 'alarmToggle' <ToggleButton>;
        //---------- Change listener;


        //binding.alarmToggle.isChecked = isServiceRunning(ContactStatusNotificationBackgroundService::class.java)

        binding.alarmToggle.setOnCheckedChangeListener(

            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                //--- A. 'alarmToggle' <ToggleButton> is on.
                val toastMsg: String = if (isChecked) {

                    //- (c) ContactStatusService for Push Notifications on.
                    requireActivity().startForegroundService(Intent(context, ContactStatusNotificationBackgroundService()::class.java))

                    //- (v) toastMsg -"on"->.
                    getString(R.string.alarm_on_toast)
                }
                //--- B. 'alarmToggle' <ToggleButton> is off.
                else {

                    //- (c) ContactStatusService for Push Notifications off.
                    requireActivity().stopService(Intent(context, ContactStatusNotificationBackgroundService::class.java))

                    //- (v) toastMsg -"off"->.
                    getString(R.string.alarm_off_toast)
                }
                // Show toast to say the alarm is turned on or off.
                Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
            }
        )
        //--------------------

        //-------------------- 'recyclerContactListGrid' <RecyclerView>;
        //---------- SetLayoutManager; (c) GridLayoutManager.
        val manager = GridLayoutManager(activity, 3)
        binding.recyclerViewContactListGrid.layoutManager = manager
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =  when (position) {
                0 -> 3
                else -> 1
            }
        }
        //---------- OnClicked; (c) ContactListListener of (c) ContactListAdapter.
        val adapter = ContactListAdapter(ContactListListener {
                contactId -> contactTrackerViewModel.onContactClicked(contactId)
        })
        //---------- SetAdapter; (c) ContactListAdapter.
        binding.recyclerViewContactListGrid.adapter = adapter
        //---------- Observer; (c) ContactListAdapter <- Watching (v) persons & non empty (v) persons.
        contactTrackerViewModel.persons.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addHeaderAndSubmitList(it)
            }
        })
        //--------------------

        //-------------------- 'Contact' <Image>;
        //---------- Observer; Navigating.
        contactTrackerViewModel.navigateToContactDetails.observe(viewLifecycleOwner, Observer {
            contactId -> contactId?.let {
                this.findNavController().navigate(ContactTrackerFragmentDirections.actionContactTrackerFragmentToContactDetailsFragment(contactId))
                contactTrackerViewModel.doneNavigatingToContactDetailsFragment()
            }
        })
        //--------------------

        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        alarmToggle.isChecked = isServiceRunning(ContactStatusNotificationBackgroundService::class.java)
    }


    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)

        for (serviceInfo in runningServices) {
            if (serviceInfo.service.className == serviceClass.name) {
                return true
            }
        }
        return false
    }


}
