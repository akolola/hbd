package com.example.android.happybirthdates.contacttracker

import android.content.Context
import android.app.AlarmManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactTrackerBinding
import android.app.PendingIntent
import android.os.PersistableBundle

private const val TAG = "ContactTrackerFragment"
private const val ALARM_REQUEST_CODE = 1111
private const val PARAMETER_KEY = "notificationFrequency"

/**
 * (c) Fragment with buttons for Contacts, which are saved in DB. Cumulative data are
 * displayed in RecyclerView.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ContactTrackerFragment : Fragment() {

    // Initialize the AlarmManager
    private var mAlarmManager : AlarmManager? = null


    //---------- (v) for Push Notifications.
    private val JOB_ID = 2222

    private lateinit var binding: FragmentContactTrackerBinding

    /**
     * The (m) is called when (c) ContactTrackerFragment is ready to display content to screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //---------- (c) ContactTrackerFragment <- |fragment layout| fragment_contact_tracker.
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_tracker, container, false)

        mAlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //--------------------------- Preparation --------------------------------------------------


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
        binding.alarmToggle.setOnCheckedChangeListener(

            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                //--- A. 'alarmToggle' <ToggleButton> is on.
                val toastMsg: String = if (isChecked) {

                    //- (c) ContactStatusService for Push Notifications on.
                    startService("30000")

                    //- (v) toastMsg -"on"->.
                    "Service started"//getString(R.string.alarm_on_toast)
                }
                //--- B. 'alarmToggle' <ToggleButton> is off.
                else {

                    //- (c) ContactStatusService for Push Notifications off.
                    stopService()

                    //- (v) toastMsg -"off"->.
                    "Service stopped"//getString(R.string.alarm_off_toast)
                }
                // Show toast to say the alarm is turned on or off.
                Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
            }
        )

        getActiveAlarm()?.let {
            binding.alarmToggle.isChecked = true
        }

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

        getActiveAlarm()?.let {
            binding.alarmToggle.isChecked = true
        }
        "Service resumed"//getString(R.string.alarm_off_toast)
    }


    private fun startService(milliseconds : String) {

        val extras = PersistableBundle().apply {
            putString(PARAMETER_KEY, milliseconds)
        }

        val jobScheduler = requireContext().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(requireContext(), AlarmStarterJobService::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName)
            .setRequiresCharging(true)
            .setPersisted(true)
            .setExtras(extras)
            .build()

        jobScheduler.schedule(jobInfo)
    }


    private fun stopService() {
        val jobScheduler = requireContext().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(JOB_ID)
    }


    // This method will return a list of currently active PendingIntent objects associated with your application alarms
    fun getActiveAlarm(): PendingIntent? {

        // Create an empty intent with a unique action as the basis for comparing pending intents
        val comparisonIntent = Intent(activity, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(activity, ALARM_REQUEST_CODE, comparisonIntent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)

        return pendingIntent
    }


}
