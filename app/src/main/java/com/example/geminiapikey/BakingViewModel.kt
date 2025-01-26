package com.example.geminiapikey

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var currentIndex: Int? = null

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.apiKey
    )
    fun refreshPrompt(index: Int, prompt: String, image: Bitmap?) {
        currentIndex = index
        _uiState.value = UiState.Loading
        // Simulate a network call or processing
        viewModelScope.launch {
            delay(1000)  // Simulate delay
            sendPrompt(image, prompt)  // Replace with actual logic
        }
    }
    fun sendPrompt(bitmap: Bitmap?, prompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Generate content once instead of streaming
                val response = generativeModel.generateContent(
                    content {
                        if (bitmap != null) {
                            image(bitmap)
                        }
                        text(prompt)
                    }
                )

                // Process the response text
                val formattedContent = formatHeadings(response.text ?: "")

                // Update UI with the full response once generated
                withContext(Dispatchers.Main) {
                    _uiState.value = UiState.Success(formattedContent, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "An error occurred")
                }
            }
        }
    }
}

private fun formatHeadings(text: String): String {
    return text.replace(Regex("<b>(.*?)</b>"), "**$1**")
}



