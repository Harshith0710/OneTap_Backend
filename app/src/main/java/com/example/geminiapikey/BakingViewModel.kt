package com.example.geminiapikey

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var currentIndex: Int? = null

    // Hardcoded predefined conversation history
    private val predefinedHistory = listOf(
        "From now on, I refer to you as KIRA. Do not include your name in each response, just say it when i ask you",
        "Okay I understand. I will be your KIRA and be friendly."
    )

    // Stores conversation history (Mutable)
    private val conversationHistory = mutableListOf<String>().apply {
        addAll(predefinedHistory)
    }

    private val _displayedTextMap = mutableStateMapOf<Int, String>()
    val displayedTextMap: SnapshotStateMap<Int, String> get() = _displayedTextMap

    // Store whether typing is complete for each response
    private val _isTypingCompleteMap = mutableStateMapOf<Int, Boolean>()
    val isTypingCompleteMap: SnapshotStateMap<Int, Boolean> get() = _isTypingCompleteMap

    // Update the displayed text for a specific response
    fun updateDisplayedText(index: Int, text: String) {
        _displayedTextMap[index] = text
    }

    // Mark typing as complete for a specific response
    fun markTypingComplete(index: Int) {
        _isTypingCompleteMap[index] = true
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = BuildConfig.apiKey,
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
        )
    )

    fun refreshPrompt(index: Int, prompt: String, image: Bitmap?) {
        currentIndex = index
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            sendPrompt(image, prompt)
        }
    }

    fun sendPrompt(bitmap: Bitmap?, prompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Append new user input to conversation history
                conversationHistory.add("User: $prompt")


                // Generate response with full conversation context
                val response = generativeModel.generateContent(
                    content {
                        if (bitmap != null) {
                            image(bitmap)
                        }
                        text(conversationHistory.joinToString("\n"))
                    }
                )

                val formattedContent = formatHeadings(response.text ?: "")

                // Add AI response to conversation history
                conversationHistory.add(formattedContent)

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
