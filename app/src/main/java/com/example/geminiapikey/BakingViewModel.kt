package com.example.geminiapikey

import UiState
import android.graphics.Bitmap
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

    fun sendPrompt(bitmap: Bitmap, prompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val accumulatedContent = StringBuilder()

                generativeModel.generateContentStream(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                ).collect { responseChunk ->
                    responseChunk.text?.let { partialContent ->
                        accumulatedContent.append(partialContent)
                        // Update UI with the current streamed content
                        _uiState.value = UiState.Streaming(accumulatedContent.toString())
                    }
                }

                // Final state after the stream completes
                _uiState.value = UiState.Success(accumulatedContent.toString())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }
}
