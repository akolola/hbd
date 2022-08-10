package com.example.android.happybirthdates.contactbackup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactBackupBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_contact_backup.*


private val TAG = "ContactBackupFragment"



class ContactBackupFragment : Fragment(), View.OnClickListener {

    companion object {
        private val RC_SIGN_IN = 9001
    }

    private val mApplication = requireNotNull(this.activity).application
    private var mAuth: FirebaseAuth? = null
    internal lateinit var mGoogleSignInClient: GoogleSignInClient
    ///private lateinit var viewModel: ContactBackupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        //--------------------------- Declaration --------------------------------------------------
        //---------- (c) ContactBackupFragment <- |fragment layout| fragment_contact_backup.
        val binding: FragmentContactBackupBinding  = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_backup, container, false)

        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- (c) ContactTrackerFragment <- |DB| ContactDatabase.
        val database = ContactDatabase.getInstance(application).contactDatabaseDao


        //--------------------------- Processing ---------------------------------------------------
        //-------------------- 'GoogleSignIn' <Button>;
        binding.signInButton.setOnClickListener(this)

        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }

    override fun onClick(v: View) {
        when(v.id){
            sign_in_button.id -> signInToGoogle()
        }
    }

    fun signInToGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onStart() {
        super.onStart()

        val currentUser = mAuth!!.currentUser

        if(currentUser!=null){
            Log.d(TAG,"Currently signed in: " + currentUser.email!!)
            Toast.makeText(mApplication, "Google logged in: "+ currentUser.email!!, Toast.LENGTH_LONG).show()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Toast.makeText(mApplication,"Google sign in succeeded",Toast.LENGTH_LONG).show()
                //firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException){
                Log.w(TAG,"Google sign in failed", e)
                Toast.makeText(mApplication,"Google sign in failed $e",Toast.LENGTH_LONG).show()
            }
        }

    }

}

