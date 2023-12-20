package com.arifin.capstone.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.arifin.capstone.R
import com.arifin.capstone.databinding.ActivityLoginBinding
import com.arifin.capstone.ui.homepage.MainActivity
import com.arifin.capstone.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }

        binding.googleBtn.setOnClickListener {
            signIn()
        }

        binding.buttonLogin.setOnClickListener {
            if (!validateUsername() || !validatePassword()) {

            } else {
                checkUser()
            }
        }
    }

    // validasi untuk user login
    private fun validateUsername(): Boolean {
        val valUsername = binding.emailLogin.text.toString()
        return if (valUsername.isEmpty()) {
            binding.emailLogin.error = "Email cannot be empty"
            false
        } else {
            binding.emailLogin.error = null
            true
        }
    }

    private fun validatePassword(): Boolean {
        val valPassword = binding.passwordLogin.text.toString()
        return if (valPassword.isEmpty()) {
            binding.passwordLogin.error = "Password cannot be empty"
            false
        } else {
            binding.passwordLogin.error = null
            true
        }
    }

//    private fun checkUser() {
//        val userUsername = binding.emailLogin.text.toString().trim()
//        val userPassword = binding.passwordLogin.text.toString().trim()
//
//        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
//        val checkUserDatabase: Query = reference.orderByChild("username").equalTo(userUsername)
//
//        checkUserDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    val userSnapshot = snapshot.children.first()
//                    val passwordFromDB = userSnapshot.child("password").getValue(String::class.java)?.trim()
//                    if (passwordFromDB == hashPassword(userPassword)) {
//
//                        binding.emailLogin.error = null
//
//                        val nameFromDB = snapshot.child(userUsername).child("name").getValue(String::class.java)
//                        val emailFromDB = snapshot.child(userUsername).child("email").getValue(String::class.java)
//                        val usernameFromDB = snapshot.child(userUsername).child("username").getValue(String::class.java)
//
//                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
//
//                        intent.putExtra("name", nameFromDB)
//                        intent.putExtra("email", emailFromDB)
//                        intent.putExtra("username", usernameFromDB)
//                        intent.putExtra("password", passwordFromDB)
//
//                        startActivity(intent)
//
//                        finish()
//
//                    } else {
//                        binding.passwordLogin.error = "Invalid Credentials"
//                        binding.passwordLogin.requestFocus()
//                    }
//                } else {
//                    binding.emailLogin.error = "User does not exist"
//                    binding.emailLogin.requestFocus()
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//    }

    private fun checkUser() {
        val userUsername = binding.emailLogin.text.toString().trim()
        val userPassword = binding.passwordLogin.text.toString().trim()

        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        val checkUserDatabase: Query = reference.orderByChild("username").equalTo(userUsername)

        checkUserDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userSnapshot = snapshot.children.first()
                    val uid = userSnapshot.key // Mendapatkan UID dari snapshot
                    val passwordFromDB = userSnapshot.child("password").getValue(String::class.java)?.trim()

                    if (passwordFromDB == hashPassword(userPassword)) {
                        binding.emailLogin.error = null

                        val usernameFromDB = userSnapshot.child("username").getValue(String::class.java)
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)

                        intent.putExtra("username", usernameFromDB)
                        intent.putExtra("uid", uid) // Menambahkan UID ke intent

                        startActivity(intent)
                        finish()
                    } else {
                        binding.passwordLogin.error = "Invalid Credentials"
                        binding.passwordLogin.requestFocus()
                    }
                } else {
                    binding.emailLogin.error = "User does not exist"
                    binding.emailLogin.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

//    login dengan google firebase
    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}