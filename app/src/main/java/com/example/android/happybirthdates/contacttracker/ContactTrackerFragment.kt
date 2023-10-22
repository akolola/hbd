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
import android.util.Log
import android.widget.SeekBar



/**
 * (c) Fragment with buttons for Contacts, which are saved in DB. Cumulative data are
 * displayed in RecyclerView.
 */
@RequiresApi(Build.VERSION_CODES.O)
class ContactTrackerFragment : Fragment() {


    companion object {
        private const val TAG = "ContactTrackerFragment"
        private const val ALARM_REQUEST_CODE = 1111
        private const val JOB_ID = 2222
        private const val PARAMETER_KEY = "notificationFrequency"
        private const val INTERVAL_1_MIN = "60000"
        private const val INTERVAL_1_HOUR = "3600000"
        private const val INTERVAL_6_HOURS = "21600000"
        private const val INTERVAL_12_HOURS = "43200000"
    }


    //---------- Initialize (c') AlarmManager.
    private var mAlarmManager : AlarmManager? = null

    //---------- Initialize (c) ContactTrackerFragment.
    private lateinit var mBinding: FragmentContactTrackerBinding


    /**
     * The (m) is called when (c) ContactTrackerFragment is ready to display content to screen.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Preparation --------------------------------------------------
        //---------- (c) ContactTrackerFragment <- |fragment layout| fragment_contact_tracker.
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_tracker, container, false)

        //---------- (c') AlarmManager.
        mAlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- (c) ContactTrackerFragment <- |DB| ContactDatabase.
        val dbPerson = ContactDatabase.getInstance(application).contactDatabaseDao

        //---------- (c) ContactDetailsViewModel <- |navigation| (v)s args: (v) isContactDeleted & (v) database & (v) application.
        val  viewModelFactory = if (arguments != null){
            ContactTrackerViewModelFactory(ContactTrackerFragmentArgs.fromBundle(requireArguments()).isContactDeleted, dbPerson, application)
        } else{
            ContactTrackerViewModelFactory(false, dbPerson, application)
        }

        //---------- (c) ContactDetailsViewModel <-  (c) ContactTrackerViewModel.
        val contactTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(ContactTrackerViewModel::class.java)

        //---------- (v) arg: (v) millisecondsBetweenNotifications -> (c) AlarmStarterJobService.
        var millisecondsBetweenNotifications : String = INTERVAL_1_HOUR


        //--------------------------- Processing ---------------------------------------------------
        mBinding.contactTrackerViewModel = contactTrackerViewModel
        mBinding.lifecycleOwner = this

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
        mBinding.alarmToggle.setOnCheckedChangeListener(

            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                //--- A. 'alarmToggle' <ToggleButton> is on.
                val toastMsg: String = if (isChecked) {

                    //- (c) ContactStatusService for Push Notifications on.
                    startService(millisecondsBetweenNotifications)

                    //- Disable 'seekBarNotificationFrequency' <SeekBar>
                    mBinding.seekBarNotificationFrequency.isEnabled = false
                    mBinding.seekBarNotificationFrequency.alpha = 0.5f

                    //- (v) toastMsg -"on"->.
                    "Service started"

                }
                //--- B. 'alarmToggle' <ToggleButton> is off.
                else {

                    //- (c) ContactStatusService for Push Notifications off.
                    stopService()

                    //- Enable 'seekBarNotificationFrequency' <SeekBar>
                    mBinding.seekBarNotificationFrequency.isEnabled = true
                    mBinding.seekBarNotificationFrequency.alpha = 1F

                    //- (v) toastMsg -"off"->.
                    "Service stopped"
                }
                // Show toast to say the alarm is turned on or off.
                Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, toastMsg)
            }
        )

        getActiveAlarm()?.let {
            mBinding.alarmToggle.isChecked = true
        }
        //--------------------

        //-------------------- 'seekBarNotificationFrequency' <SeekBar>;
        mBinding.seekBarNotificationFrequency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                when (progress) {
                    0 -> { millisecondsBetweenNotifications =  INTERVAL_1_MIN
                        mBinding.textViewNotificationFrequency.text = getString(R.string.notification_1_min)
                    }
                    1 -> { millisecondsBetweenNotifications = INTERVAL_1_HOUR
                        mBinding.textViewNotificationFrequency.text = getString(R.string.notification_1_hr)
                    }
                    2 -> { millisecondsBetweenNotifications = INTERVAL_6_HOURS
                        mBinding.textViewNotificationFrequency.text = getString(R.string.notification_6_hrs)
                    }
                    3 -> { millisecondsBetweenNotifications = INTERVAL_12_HOURS
                        mBinding.textViewNotificationFrequency.text = getString(R.string.notification_12_hrs)
                    }
                    else -> {
                        mBinding.textViewNotificationFrequency.text = getString(R.string.notification_na)
                    }
                }

            }

            //---------- (m)s to be empty
            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        })
        //--------------------

        //-------------------- 'recyclerContactListGrid' <RecyclerView>;
        //---------- SetLayoutManager; (c) GridLayoutManager.
        val manager = GridLayoutManager(activity, 3)
        mBinding.recyclerViewContactListGrid.layoutManager = manager
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
        mBinding.recyclerViewContactListGrid.adapter = adapter
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
        return mBinding.root

    }


    /**
     * Overridden (m) checks if (c) AlarmStarterJobService is active or not, with push up notifications
     */
    override fun onResume() {
        super.onResume()

        getActiveAlarm()?.let {
            mBinding.alarmToggle.isChecked = true
        }

        Toast.makeText(requireContext(), getString(R.string.alarm_off_toast), Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Service resumed")


    }


    /**
     * Starts (c) AlarmStarterJobService, which sets up push up notifications
     *
     * @param milliseconds frequency in milliseconds how often push up notifications should be displayed
     *
     */
    private fun startService(milliseconds : String) {

        val extras = PersistableBundle().apply {
            putString(PARAMETER_KEY, milliseconds)
        }

        val jobScheduler = requireContext().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(requireContext(), AlarmStarterJobService::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).setRequiresCharging(true).setPersisted(true).setExtras(extras).build()

        jobScheduler.schedule(jobInfo)

    }


    /**
     * Stops (c) AlarmStarterJobService, all push up notifications realted to service are not displayed anymore
     *
     */
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
