package com.example.geminiapikey


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.geminiapikey.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the correct binding class
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set OnClickListener for the reset password button
        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            if (email.isEmpty()) {
                // Show an error message if email is empty
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                // Send password reset email
                resetPassword(email)
            }
        }
    }

    // Function to reset the password
    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    // Navigate to Reset Confirmation Screen
                    startActivity(Intent(this, ResetConfirmationActivity::class.java))
                    finish() // Close this activity
                } else {
                    // Show error message if the task fails
                    Toast.makeText(this, "Failed to send reset email. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}