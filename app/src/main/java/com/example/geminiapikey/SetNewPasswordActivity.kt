package com.example.geminiapikey


import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.geminiapikey.databinding.SetNewPasswordBinding

class SetNewPasswordActivity : ComponentActivity() {
    private lateinit var binding: SetNewPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SetNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirmPasswordButton.setOnClickListener {
            // Implement password update logic here
        }
    }
}