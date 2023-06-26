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
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.android.synthetic.main.fragment_contact_backup.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*




private const val TAG = "ContactBackupFragment"
private const val BACKUP_DIR_NAME = "HBD_Backup"
private const val BACKUP_FILE_NAME = "special_day_database"
private const val BACKUP_FILE_NAME_SHM  = "special_day_database-shm"
private const val BACKUP_FILE_NAME_WAL  = "special_day_database-wal"

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

            mDrive = getDriveService(this.requireContext())
            uploadFileToGoogleDrive(requireContext(), BACKUP_DIR_NAME)

        }

        //-------------------- 'buttonSave' <Button>;
        //---------- Click listener; (c) ContactBackupFragment -[|DB| Contact]-> GoogleDrive.
        binding.buttonLoad.setOnClickListener {

            mDrive = getDriveService(this.requireContext())
            downloadBackupFromGoogleDrive(requireContext())

        }

        return binding.rootView
    }

    private fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let {
            googleAccount ->   val credential = GoogleAccountCredential.usingOAuth2(this.context, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA))
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName(getString(R.string.app_name)).build()
        }
    }


    // ****
    private fun uploadFileToGoogleDrive(context: Context, fileName: String) {

        Log.d(TAG,"In (m) setOnClickListener")
        ///Toast.makeText(context, "In (m) setOnClickListener", Toast.LENGTH_LONG).show()

        mDrive.let { googleDriveService ->
            lifecycleScope.launch {

                val folderMetadata = File()
                folderMetadata.name = BACKUP_DIR_NAME
                folderMetadata.mimeType = "application/vnd.google-apps.folder"

                val fileMetadata1 = File()
                fileMetadata1.name = BACKUP_FILE_NAME

                val fileMetadata2 = File()
                fileMetadata2.name = BACKUP_FILE_NAME_SHM

                val fileMetadata3 = File()
                fileMetadata3.name = BACKUP_FILE_NAME_WAL

                val content1 = java.io.File("/data/user/0/com.example.android.happybirthdates/databases/special_day_database")
                val mediaContent1 = FileContent("application/x-sqlite3", content1)
                val content2 = java.io.File("/data/user/0/com.example.android.happybirthdates/databases/special_day_database-shm")
                val mediaContent2 = FileContent("application/x-sqlite3", content2)
                val content3 = java.io.File("/data/user/0/com.example.android.happybirthdates/databases/special_day_database-wal")
                val mediaContent3 = FileContent("application/x-sqlite3", content3)


                //---------- Find & delete backup dir and its all possible copies
                withContext(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        launch {

                            var availableDirIdList: MutableList<String> = getBackupDBDirIdListInGoogleDrive(googleDriveService)
                            deleteDirListByIdInGoogleDrive(availableDirIdList, googleDriveService)

                        }
                    }
                }


                //---------- Create Files 1,2,3
                withContext(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        launch {

                            val dir = googleDriveService.files().create(folderMetadata).setFields("id").execute()

                            Log.d(TAG, "Created directory: name=${dir.name}, id=${dir.id}")

                            fileMetadata1.parents = listOf(dir.id)
                            val file1 = googleDriveService.files().create(fileMetadata1, mediaContent1).setFields("id").execute()
                            Log.d(TAG, "Uploaded file: name=${file1.name}, id=${file1.id}")

                            fileMetadata2.parents = listOf(dir.id)
                            val file2 = googleDriveService.files().create(fileMetadata2, mediaContent2).setFields("id").execute()
                            Log.d(TAG, "Uploaded file: name=${file2.name}, id=${file2.id}")

                            fileMetadata3.parents = listOf(dir.id)
                            val file3 = googleDriveService.files().create(fileMetadata3, mediaContent3).setFields("id").execute()
                            Log.d(TAG, "Uploaded file: name=${file3.name}, id=${file3.id}")

                        }
                    }
                }


           }

       }

    }

    private fun getBackupDBDirIdListInGoogleDrive(googleDriveService: Drive): MutableList<String> {
        var availableDirIdList: MutableList<String> = mutableListOf()
        val result = googleDriveService.files().list().setQ("'root' in parents and mimeType='application/vnd.google-apps.folder' and trashed = false").setFields("nextPageToken, files(id, name)").execute()
        val dirList: List<File> = result.files
        for (dir in dirList) {
            if (dir.name == BACKUP_DIR_NAME) {
                availableDirIdList.add(dir.id)
            }
        }
        return availableDirIdList
    }

    private fun deleteDirListByIdInGoogleDrive(availableDirIdList: MutableList<String>, googleDriveService: Drive) {
        if (availableDirIdList.isNotEmpty()) {
            for (dirId in availableDirIdList) {
                try {
                    googleDriveService.files().delete(dirId).execute()
                    Log.d(TAG, "Dir with id $dirId deleted successfully.")
                } catch (e: IOException) {
                    Log.d(TAG, "An error occurred: $e")
                }
            }
        } else {
            Log.d(TAG, "No directories found with name $BACKUP_DIR_NAME.")
        }
    }


    // ****
    private fun downloadBackupFromGoogleDrive(context: Context) {

        mDrive.let { googleDriveService ->
            lifecycleScope.launch {

                withContext(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        launch {

                            val availableDirIdList = getBackupDBDirIdListInGoogleDrive(googleDriveService)

                            if(availableDirIdList.isEmpty()){
                                Log.d(TAG, "No directories found with name $BACKUP_DIR_NAME.")
                                Toast.makeText(context, "No directories found with name $BACKUP_DIR_NAME", Toast.LENGTH_LONG).show()
                            } else{

                                var dBFileList = getDBFileListFromBackupFolder(availableDirIdList.get(0) ,googleDriveService)

                                if (dBFileList.size == 3){
                                    val dbFile1 = dBFileList.find { it.name == BACKUP_FILE_NAME }
                                    val dbFile2 = dBFileList.find { it.name == BACKUP_FILE_NAME_SHM }
                                    val dbFile3 = dBFileList.find { it.name == BACKUP_FILE_NAME_WAL }

                                    saveFileToLocalDBFolder(dbFile1!!, BACKUP_FILE_NAME, googleDriveService)
                                    saveFileToLocalDBFolder(dbFile2!!, BACKUP_FILE_NAME_SHM, googleDriveService)
                                    saveFileToLocalDBFolder(dbFile3!!, BACKUP_FILE_NAME_WAL, googleDriveService)
                                }

                            }

                        }

                    }

                }
            }
        }
    }

    fun getDBFileListFromBackupFolder(folderId: String, service: Drive): List<File> {
        var files: List<File> = emptyList()
        val query = "mimeType != 'application/vnd.google-apps.folder' and '$folderId' in parents and trashed = false"
        val request = service.files().list().setQ(query)
        do {
            val result = request.execute()
            files = result.files
            request.pageToken = result.nextPageToken
        } while (request.pageToken != null && request.pageToken.isNotEmpty())
        return files
    }

    fun saveFileToLocalDBFolder(file: File, localFilename: String, service: Drive) {

        try {

            // Determine correct path to dir for DB file, e.g. data/user/0/com.example.android.happybirthdates/databases
            val path = context?.getDatabasePath(localFilename)?.path

            // Convert the Drive API file to a File object
            val javaFile = java.io.File(path)
            val outputStream = javaFile.outputStream()

            // Download the file content and write it to the local file
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)

            // Close the output stream
            outputStream.close()

            Log.d("File", "File saved successfully.")

        } catch (e: Exception) {
            Log.e("Exception", "File write failed: $e")
        }
    }


}

