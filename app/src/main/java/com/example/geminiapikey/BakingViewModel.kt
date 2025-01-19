package com.example.geminiapikey

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.apiKey
    )

    fun sendPrompt(bitmap: Bitmap?, prompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val accumulatedContent = StringBuilder()

                generativeModel.generateContentStream(
                    content {
                        if (bitmap != null) {
                            image(bitmap)
                        }
                        text(prompt)
                    }
                ).collect { responseChunk ->
                    responseChunk.text?.let { partialContent ->
                        val formattedContent = formatHeadings(partialContent)
                        accumulatedContent.append(formattedContent)
                        // Update UI with the current streamed content
                        _uiState.value = UiState.Streaming(accumulatedContent.toString())
                    }
                }

                // Final state after the stream completes
                // Assuming that the image URI can be derived from the bitmap or another response
                val imageUri: Uri? = null // You can modify this part based on how you get the image URI
                _uiState.value = UiState.Success(accumulatedContent.toString(), imageUri)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }
}

private fun formatHeadings(text: String): String {
    return text.replace(Regex("<b>(.*?)</b>"), "**$1**")
}

