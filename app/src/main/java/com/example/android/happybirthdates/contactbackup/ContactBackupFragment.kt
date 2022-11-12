package com.example.android.happybirthdates.contactbackup

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.databinding.FragmentContactCloudBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import kotlinx.android.synthetic.main.fragment_contact_backup.view.*

class ContactBackupFragment : Fragment() {

    companion object {
        fun newInstance() = ContactBackupFragment()
        private val TAG = "BackupActivity"
        private val RC_SELECT_FILE = 9111
    }

    lateinit var mDrive: Drive

    private lateinit var viewModel: ContactBackupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Declaration --------------------------------------------------
        //---------- (c) ContactBackupFragment <- |fragment layout| fragment_contact_backup.
        val binding = inflater.inflate(R.layout.fragment_contact_backup, container, false)

        //--------------------------- Processing ---------------------------------------------------
        //-------------------- 'buttonSave' <Button>;
        //---------- Click listener; (c) ContactBackupFragment -[|DB| Contac]-> GoogleDrive.
        binding.buttonSave.setOnClickListener {
            uploadFileToGoogleDrive(requireContext(),"special_day_database")
        }

        return binding.rootView
    }

    private fun uploadFileToGoogleDrive(context: Context, fileName: String) {

        Log.w(TAG,"In (m) setOnClickListener")
        Toast.makeText(context, "In (m) setOnClickListener", Toast.LENGTH_LONG).show()
        /*
        mDrive.let { googleDriveService ->
            lifecycleScope.launch {
                try {
                    val absolutePath = requireContext().getFileStreamPath(fileName).absolutePath
                   val jpegFile = File(absolutePath)
                                       val gfile = com.google.api.services.drive.model.File()
                                       gfile.name = "examplepic"
                                       val mimetype = "image/jpeg"
                                       val fileContent = FileContent(mimetype, jpegFile)
                                       ///var fileid = ""

                                       withContext(Dispatchers.Main) {
                                           withContext(Dispatchers.IO) {
                                               launch {
                                                   var mFile = googleDriveService.Files().create(gfile, fileContent).execute()
                                               }
                                           }
                                       }

                } catch (userAuthEx: UserRecoverableAuthIOException) {
                    startActivity(
                        userAuthEx.intent
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d(TAG, "Error by file(s) upload. ", e)
                    Toast.makeText(context, "Error by file(s) upload. $e", Toast.LENGTH_LONG).show()
                }
            }
        }
        */
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


}