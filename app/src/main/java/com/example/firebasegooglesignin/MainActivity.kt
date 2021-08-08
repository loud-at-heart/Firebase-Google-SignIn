package com.example.firebasegooglesignin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.firebasegooglesignin.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    private lateinit var googleSignInClient:GoogleSignInClient
    private lateinit var firebaseAuth:FirebaseAuth

    private companion object{
        private const val RC_SIGN_IN =100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOption)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()

        binding.googleSignInBtn.setOnClickListener{
            Log.d(TAG,"onCreate: Begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    private fun checkUser() {
        var firebaseUser =firebaseAuth.currentUser
        if(firebaseUser!=null){
            startActivity(Intent(this,ProfileActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG,"onActivityResult: Google SignIn Intent Result")
        val accounTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = accounTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogleAccount(account)
        }catch (e:Exception){
            Log.d(TAG,"onActivityResult: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credentials =GoogleAuthProvider.getCredential(account!!.idToken,null)
        firebaseAuth.signInWithCredential(credentials)
            .addOnSuccessListener {
                val firebaseUser = firebaseAuth.currentUser
                val uid = firebaseUser!!.uid
                val email = firebaseUser!!.email

                if(it.additionalUserInfo!!.isNewUser){
                    Toast.makeText(this,"Account Created...\n$email",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this,"LoggedIn...\n$email",Toast.LENGTH_LONG).show()
                }
                startActivity(Intent(this,ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this,"LoggedIn Failed due to...\n${it.message}",Toast.LENGTH_LONG).show()
            }
    }
}