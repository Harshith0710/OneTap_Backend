package com.example.geminiapikey


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import androidx.activity.ComponentActivity

class MainActivity3 : ComponentActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // Initialize Firebase App and Auth
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        firebaseAuth = FirebaseAuth.getInstance()

        // Access Views
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val confirmPasswordInput: EditText = findViewById(R.id.confirm_password_input)
        val signInButton: Button = findViewById(R.id.sign_in_button)
        val errorBar: LinearLayout = findViewById(R.id.error_bar)
        val eyeButtonPassword: ImageButton = findViewById(R.id.eye_button_password)
        val eyeButtonConfirmPassword: ImageButton = findViewById(R.id.eye_button_confirm_password)

        // Hide error bar initially
        errorBar.visibility = View.GONE

        // Set Click Listener for Sign-Up Button
        signInButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Input validation
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errorBar.visibility = View.VISIBLE
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                errorBar.visibility = View.VISIBLE
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                errorBar.visibility = View.VISIBLE
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with Firebase Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        redirectToHome()
                    } else {
                        // Handle errors
                        errorBar.visibility = View.VISIBLE
                        val errorMessage = task.exception?.message ?: "Unknown error"
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    errorBar.visibility = View.VISIBLE
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Toggle Password Visibility for Password Field
        eyeButtonPassword.setOnClickListener {
            if (passwordInput.transformationMethod is PasswordTransformationMethod) {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                eyeButtonPassword.setImageResource(R.drawable.ic_openeye) // Open eye icon
            } else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeButtonPassword.setImageResource(R.drawable.ic_closedeye) // Closed eye icon
            }
        }

        // Toggle Password Visibility for Confirm Password Field
        eyeButtonConfirmPassword.setOnClickListener {
            if (confirmPasswordInput.transformationMethod is PasswordTransformationMethod) {
                confirmPasswordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                eyeButtonConfirmPassword.setImageResource(R.drawable.ic_openeye) // Open eye icon
            } else {
                confirmPasswordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeButtonConfirmPassword.setImageResource(R.drawable.ic_closedeye) // Closed eye icon
            }
        }
    }

    private fun redirectToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Optional: Close MainActivity3 to prevent back navigation
    }
}
