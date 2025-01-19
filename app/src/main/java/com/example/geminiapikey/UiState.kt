package com.example.geminiapikey

import android.net.Uri

sealed interface UiState {
    data object Initial : UiState
    data object Loading : UiState
    data class Streaming(val partialText: String) : UiState
    data class Success(val outputText: String, val imageUri: Uri? = null) : UiState
    data class Error(val errorMessage: String) : UiState
}
