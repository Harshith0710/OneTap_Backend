package com.example.geminiapikey


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.geminiapikey.databinding.ResetConfirmationBinding

class ResetConfirmationActivity : ComponentActivity() {
    private lateinit var binding: ResetConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResetConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goToLoginButton.setOnClickListener {
            startActivity(Intent(this, SetNewPasswordActivity::class.java))
        }
    }
}