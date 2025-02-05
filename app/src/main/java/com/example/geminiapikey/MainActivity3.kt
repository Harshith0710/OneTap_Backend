package com.example.geminiapikey

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity3 : ComponentActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mdbref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        firebaseAuth = FirebaseAuth.getInstance()

        // Access Views
        val nameInput: EditText = findViewById(R.id.name_input) // 🔥 Added name input
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val confirmPasswordInput: EditText = findViewById(R.id.confirm_password_input)
        val signInButton: Button = findViewById(R.id.sign_in_button)
        val errorBar: LinearLayout = findViewById(R.id.error_bar)
        val errormsg: TextView = findViewById(R.id.texterror)

        errorBar.visibility = View.GONE

        signInButton.setOnClickListener {
            val name = nameInput.text.toString().trim() // 🔥 Get name input
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Input validation
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errormsg.text = "ERROR: Please fill in all fields"
                errorBar.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                errormsg.text = "ERROR: Passwords do not match"
                errorBar.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 6) {
                errormsg.text = "ERROR: Password must be at least 6 characters"
                errorBar.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Create user in Firebase Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        addusertodatabase(name,email,firebaseAuth.currentUser?.uid!!)
                        val user = firebaseAuth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name) // 🔥 Save name in Firebase Authentication profile
                            .build()

                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                errorBar.visibility = View.GONE
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                redirectToHome()
                            } else {
                                errormsg.text = "ERROR: ${it.exception?.message}"
                                errorBar.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        errormsg.text = "ERROR: ${task.exception?.message}"
                        errorBar.visibility = View.VISIBLE
                    }
                }

        }
    }

    private fun redirectToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
    private fun addusertodatabase(name: String, email: String, uid: String,){
        mdbref = FirebaseDatabase.getInstance().getReference()
        mdbref.child("user").child(name).setValue(name,email,)


    }
}