package com.example.android.happybirthdates.contactcloud

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
import com.example.android.happybirthdates.databinding.FragmentContactCloudBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider



private const val TAG = "ContactCloudFragment"



class ContactCloudFragment : Fragment() {


   companion object {
        private val RC_SIGN_IN = 9001
    }


    private var mAuth: FirebaseAuth? = null
    internal lateinit var mGoogleSignInClient: GoogleSignInClient


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        //--------------------------- Declaration --------------------------------------------------
        //---------- (c) ContactBackupFragment <- |fragment layout| fragment_contact_backup.
        val binding: FragmentContactCloudBinding  = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_cloud, container, false)


        //---------- Technical (v) application.
        val application = requireNotNull(this.activity).application

        //---------- |navigation| navigation's (v) args.
        //--

        //---------- (c) ContactTrackerFragment <- |DB| ContactDatabase.
        //--


        //--------------------------- Processing ---------------------------------------------------
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.googleClientId)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(application!!, gso)
        mAuth = FirebaseAuth.getInstance()


        //-------------------- 'GoogleSignIn' <Button>;
        binding.signInButton.setOnClickListener{
            signInToGoogle()
        }

        //-------------------- 'GoogleSignOut' <Button>;
        binding.signOutButton.setOnClickListener{
            signOutFromGoogle()
        }

        //-------------------- 'Disconnect' <Button>;
        binding.disconnectButton.setOnClickListener{
            revokeAccess()
        }



        //--------------------------- Finish -------------------------------------------------------
        return binding.root
    }

    private fun signInToGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOutFromGoogle(){
        mAuth!!.signOut()
        mGoogleSignInClient.signOut()
        Log.w(TAG, "Signed ouf of Google")
        Toast.makeText(requireActivity(),"Signed ouf of Google",Toast.LENGTH_LONG).show()
    }

    private fun revokeAccess(){
        mAuth!!.signOut()
        mGoogleSignInClient.revokeAccess()
        Log.w(TAG, "Revoked Access")
        Toast.makeText(requireActivity(),"Revoked Access",Toast.LENGTH_LONG).show()
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
                Log.w(TAG,"Google sign in succeeded")
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

