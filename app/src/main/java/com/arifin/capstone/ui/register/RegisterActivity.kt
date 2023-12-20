package com.arifin.capstone.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arifin.capstone.databinding.ActivityRegisterBinding
import com.arifin.capstone.helper.HelperClass
import com.arifin.capstone.ui.login.LoginActivity
import com.google.firebase.database.*
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }

        binding.buttonRegister.setOnClickListener {
            database = FirebaseDatabase.getInstance()
            reference = database.getReference("users")

            val name = binding.name.text.toString()
            val email = binding.emailRegister.text.toString()
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()


            val hashedPassword = hashPassword(password)

            // Check if email is already registered
            checkEmailExists(email, name, username, hashedPassword)
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun checkEmailExists(email: String, name: String, username: String, password: String) {
        val checkEmailDatabase: Query = reference.orderByChild("email").equalTo(email)

        checkEmailDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Email is already registered
                    Toast.makeText(this@RegisterActivity, "Email is already registered!", Toast.LENGTH_SHORT).show()
                } else {
                    // Email is not registered, proceed with checking username
                    checkUsernameExists(username, name, email, password)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun checkUsernameExists(username: String, name: String, email: String, password: String) {
        val checkUsernameDatabase: Query = reference.orderByChild("username").equalTo(username)

        checkUsernameDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Username is already registered
                    Toast.makeText(this@RegisterActivity, "Username is already taken!", Toast.LENGTH_SHORT).show()
                } else {
                    // Username is not registered, proceed with registration
                    registerUser(name, email, username, password)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun registerUser(name: String, email: String, username: String, password: String) {
        // Perform the user registration
        val helperClass = HelperClass(name, email, username, password)
        reference.child(name).setValue(helperClass)

        Toast.makeText(this@RegisterActivity, "You have signed up successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}