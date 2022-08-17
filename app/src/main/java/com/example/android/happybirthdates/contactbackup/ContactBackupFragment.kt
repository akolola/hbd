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
import com.example.android.happybirthdates.contactdetails.ContactDetailsFragmentArgs
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.databinding.FragmentContactBackupBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_contact_backup.*


private val TAG = "ContactBackupFragment"



class ContactBackupFragment : Fragment() {

   companion object {
        private val RC_SIGN_IN = 9001
    }

    private var mAuth: FirebaseAuth? = null
    internal lateinit var mGoogleSignInClient: GoogleSignInClient


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        //--------------------------- Declaration --------------------------------------------------
        //---------- (c) ContactBackupFragment <- |fragment layout| fragment_contact_backup.
        val binding: FragmentContactBackupBinding  = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_backup, container, false)


        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- |navigation| navigation's (v) args.
        //--

        //---------- (c) ContactTrackerFragment <- |DB| ContactDatabase.
        //--


        //--------------------------- Processing ---------------------------------------------------
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("465961236651-soqlh8n5fkqt0c2ufrvel5j5178vkchu.apps.googleusercontent.com").requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(application!!, gso)
        mAuth = FirebaseAuth.getInstance()


        //-------------------- 'GoogleSignIn' <Button>;
        binding.signInButton.setOnClickListener{
            signInToGoogle()
        }


        //--------------------------- Finish -------------------------------------------------------
        return binding.root
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
            Toast.makeText(requireActivity(), "Google logged in: "+ currentUser.email!!, Toast.LENGTH_LONG).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Toast.makeText(requireActivity(),"Google sign in succeeded",Toast.LENGTH_LONG).show()
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException){
                Log.w(TAG,"Google sign in failed", e)
                Toast.makeText(requireActivity(),"Google sign in failed $e",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        Log.d(TAG,"firebaseAuthWithGoogle: " + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential).addOnCompleteListener(requireActivity()){ task ->
            if(task.isSuccessful){
                val user = mAuth!!.currentUser
                Log.d(TAG,"(m) signInWithCredential: success: currentUser: "+user!!.email!!)
                Toast.makeText(requireActivity(),"Firebase authentication succeeded", Toast.LENGTH_LONG).show()
            } else{
                Log.w(TAG,"(m) signInWithCredential: failure")
                Toast.makeText(requireActivity(),"Firebase authentication failed", Toast.LENGTH_LONG).show()
            }

        }

    }

}

