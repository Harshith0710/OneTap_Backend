sealed interface UiState {
    object Initial : UiState
    object Loading : UiState
    data class Streaming(val partialText: String) : UiState
    data class Success(val outputText: String) : UiState
    data class Error(val errorMessage: String) : UiState
}
