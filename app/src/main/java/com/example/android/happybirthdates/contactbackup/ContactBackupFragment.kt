package com.example.android.happybirthdates.contactbackup

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.android.happybirthdates.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive

class ContactBackupFragment : Fragment() {

    companion object {
        fun newInstance() = ContactBackupFragment()
        private val TAG = "BackupActivity"
        private val RC_SELECT_FILE = 9111
    }

    lateinit var mDrive: Drive

    private lateinit var viewModel: ContactBackupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact_backup, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactBackupViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun uploadFileToGoogleDrive(context: Context, fileName: String) {
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
    }

}