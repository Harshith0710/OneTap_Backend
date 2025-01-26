package com.example.geminiapikey

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp // Import FirebaseApp for initialization

class MainActivity5 : ComponentActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)

        // Initialize FirebaseApp (this will initialize Firebase services like Authentication)
        if (!::firebaseAuth.isInitialized) {
            FirebaseApp.initializeApp(this)
        }

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signupButton = findViewById(R.id.signupButton)
        errorTextView = findViewById(R.id.errorTextView)

        // Set click listener for sign-up button
        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                errorTextView.text = "Please enter both email and password."
                errorTextView.visibility = TextView.VISIBLE
            } else {
                createAccount(email, password)
            }
        }
    }

    // Function to create a new user account using email and password in Firebase
    private fun createAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-up successful
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate to the next activity (e.g., a dashboard or home screen)
                    // startActivity(Intent(this, HomeActivity::class.java))
                    // finish()
                } else {
                    // If sign-up fails, display the error message
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Log.e("FirebaseAuth", "Sign-up failed: $errorMessage")
                    errorTextView.text = "Account creation failed: $errorMessage"
                    errorTextView.visibility = TextView.VISIBLE
                }
            }
    }
}