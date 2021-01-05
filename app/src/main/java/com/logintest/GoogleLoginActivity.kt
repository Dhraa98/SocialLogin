package com.logintest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.logintest.databinding.ActivityGloginBinding

class GoogleLoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityGloginBinding
    private val RC_SIGN_IN = 1


    private var mGoogleSignInClient: GoogleSignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=DataBindingUtil.setContentView(this,R.layout.activity_glogin)
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.signInButton.setOnClickListener {
            signIn()
        }
        binding.logout.setOnClickListener {
            mGoogleSignInClient?.signOut()?.addOnCompleteListener(this, object :
                OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {
                    Toast.makeText(this@GoogleLoginActivity, "Signed Out", Toast.LENGTH_LONG).show()
                }
            })
            finish()
        }

    }
    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateUI(account)
            binding.signInButton.visibility = View.GONE
            binding.logout.visibility = View.VISIBLE
        }else{
            binding.signInButton.visibility = View.VISIBLE
        }
    }

    private fun signIn() {
        val intent = mGoogleSignInClient?.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)


            handleSignInResult(task)


        }
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            updateUI(account)
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {

            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            binding.signInButton.visibility = View.GONE
            binding.logout.visibility = View.VISIBLE
            val personName = account.displayName
            binding.personName.setText(personName)
            val personEmail = account.email
            binding.personEmail.setText(personEmail)
            val personId = account.id
            binding.personId.setText(personId)
            // val personPhoto: Uri? = account.photoUrl
        }

    }
}