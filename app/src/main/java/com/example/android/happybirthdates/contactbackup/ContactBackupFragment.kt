package com.example.android.happybirthdates.contactbackup

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.android.happybirthdates.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.fragment_contact_backup.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



private const val TAG = "ContactBackupFragment"



class ContactBackupFragment : Fragment() {


    lateinit var mDrive: Drive
    ///private lateinit var viewModel: ContactBackupViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //--------------------------- Declaration --------------------------------------------------
        //---------- (c) ContactBackupFragment <- |fragment layout| fragment_contact_backup.
        val binding = inflater.inflate(R.layout.fragment_contact_backup, container, false)


        //--------------------------- Processing ---------------------------------------------------
        //-------------------- 'buttonSave' <Button>;
        //---------- Click listener; (c) ContactBackupFragment -[|DB| Contact]-> GoogleDrive.
        binding.buttonSave.setOnClickListener {
            //----------
            mDrive = getDriveService(this.requireContext())
            uploadFileToGoogleDrive(requireContext(),"special_day_database")
        }

        return binding.rootView
    }

    private fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let {
            googleAccount ->   val credential = GoogleAccountCredential.usingOAuth2(this.context, listOf(DriveScopes.DRIVE_FILE))
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName(getString(R.string.app_name)).build()
        }
    }


    private fun uploadFileToGoogleDrive(context: Context, fileName: String) {

        Log.w(TAG,"In (m) setOnClickListener")
        Toast.makeText(context, "In (m) setOnClickListener", Toast.LENGTH_LONG).show()

        mDrive.let { googleDriveService ->
            lifecycleScope.launch {

               try {

                   val dbFile = context?.getDatabasePath("special_day_database")
                   val mimetype = "application/vnd.sqlite3"
                   val gfile = com.google.api.services.drive.model.File()
                   gfile.name = "special_day_database"

                   val fileContent = FileContent(mimetype, dbFile)

                  withContext(Dispatchers.Main) {
                      withContext(Dispatchers.IO) {
                          launch {
                              var mFile = googleDriveService.Files().create(gfile, fileContent).execute()
                          }
                      }
                  }

               } catch (userAuthEx: UserRecoverableAuthIOException) {
                   startActivity(userAuthEx.intent)
               } catch (e: Exception) {
                   e.printStackTrace()
                   Log.d(TAG, "Error by file(s) upload. ", e)
                   Toast.makeText(context, "Error by file(s) upload. $e", Toast.LENGTH_LONG).show()
               }

           }
       }

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


}