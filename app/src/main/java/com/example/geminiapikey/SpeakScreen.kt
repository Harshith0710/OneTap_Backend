package com.example.geminiapikey

import UiState
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlin.random.Random

@Composable
fun SpeakScreenWrapper() {
    RequestPermission(android.Manifest.permission.RECORD_AUDIO) {
        SpeakScreen()
    }
}

@Composable
fun RequestPermission(permission: String, onGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    val permissionState = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState.value = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permission is required to use the microphone.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permission)
    }

    if (permissionState.value) {
        onGranted()
    }
}

@Composable
fun SpeakScreen(viewModel: BakingViewModel = viewModel()) {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var displayedText by remember { mutableStateOf("") }
    var partialText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var amplitude by remember { mutableFloatStateOf(0.1f) }
    var isSpeaking by remember { mutableStateOf(false) }
    var currentTTSChunk by remember { mutableStateOf("") }

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(context, "TTS Initialization Failed", Toast.LENGTH_SHORT).show()
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking = true
                }

                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    currentTTSChunk = ""
                    displayedText = "" // Clear displayed text when TTS is done
                }

                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    currentTTSChunk = ""
                    displayedText = "" // Clear displayed text on TTS error
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    currentTTSChunk = viewModel.uiState.value.let {
                        when (it) {
                            is UiState.Success -> it.outputText.substring(start, end)
                            is UiState.Streaming -> it.partialText.substring(start, end)
                            else -> ""
                        }
                    }
                }
            })
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
            tts.shutdown()
        }
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
        }

        override fun onBeginningOfSpeech() {
            isListening = true
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            isListening = false
        }

        override fun onError(error: Int) {
            isListening = false
            Toast.makeText(context, getErrorMessage(error), Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0) ?: ""
            spokenText = result
            displayedText = result
            partialText = ""
            viewModel.sendPrompt(null, result)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            partialText = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0) ?: ""
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(ContextCompat.getColor(context, R.color.splash_background)))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = partialText,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = displayedText,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        AnimatedLines(amplitude = if (isSpeaking) amplitude else 1f, isSpeaking = isSpeaking)

        Text(
            text = if (isSpeaking) currentTTSChunk else "",
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(50.dp), // Set the size of the outer Box
                contentAlignment = Alignment.Center // Center-align the content inside the Box
            ) {
                Image(
                    painter = painterResource(R.drawable.ellipse_84),
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize() // Fill the outer Box
                )
                Image(
                    painter = painterResource(R.drawable.keyboard_10_1),
                    contentDescription = "",
                    modifier = Modifier.size(25.dp) // Size for the inner image
                )
            }
            CircularPulseAnimation(
                isListening = isListening,
                isSpeaking = isSpeaking,
                speechRecognizer = speechRecognizer,
                speechRecognizerIntent = speechRecognizerIntent,
                tts = tts,
                onPermissionDenied = {
                    Toast.makeText(context, "Permission denied. Please enable microphone access.", Toast.LENGTH_SHORT).show()
                }
            )
            Box(
                modifier = Modifier.size(50.dp), // Set the size of the outer Box
                contentAlignment = Alignment.Center // Center-align the content inside the Box
            ) {
                Image(
                    painter = painterResource(R.drawable.ellipse_85),
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize() // Fill the outer Box
                )
                Image(
                    painter = painterResource(R.drawable.fi_rr_cross_small_1_1),
                    contentDescription = "",
                    modifier = Modifier.size(25.dp) // Size for the inner image
                )
            }
        }
    }

    when (val state = uiState) {
        is UiState.Success -> {
            LaunchedEffect(state) {
                amplitude = 0.6f
                tts.speak(state.outputText, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            }
        }

        is UiState.Streaming -> {
            LaunchedEffect(state) {
                amplitude = 0.6f
                tts.speak(state.partialText, TextToSpeech.QUEUE_ADD, null, "utteranceId")
            }
        }

        is UiState.Error -> {
            LaunchedEffect(state) {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        else -> {}
    }
}

@Composable
fun AnimatedLines(amplitude: Float, isSpeaking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Irregular Voice Lines")
    val lineCount = 10
    val staticAmplitudes = listOf(0.3f, 0.5f, 0.7f, 0.4f, 0.6f, 0.8f, 0.5f, 0.7f, 0.6f, 0.4f)
    val lineHeights = if (isSpeaking) {
        List(lineCount) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = Random.nextInt(300, 700), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Irregular Line Height"
            )
        }
    } else {
        staticAmplitudes.map { mutableFloatStateOf(it) }
    }

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
    ) {
        val lineWidth = size.width / (lineCount * 2)
        val spaceBetween = size.width / lineCount

        lineHeights.forEachIndexed { index, animation ->
            val height by animation
            val dynamicHeight = height * amplitude * size.height / 2
            drawLine(
                color = Color.Magenta,
                start = Offset(
                    x = spaceBetween * index + lineWidth / 2,
                    y = size.height / 2 - dynamicHeight
                ),
                end = Offset(
                    x = spaceBetween * index + lineWidth / 2,
                    y = size.height / 2 + dynamicHeight
                ),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun CircularPulseAnimation(
    isListening: Boolean,
    isSpeaking: Boolean,
    speechRecognizer: SpeechRecognizer,
    speechRecognizerIntent: Intent,
    tts: TextToSpeech,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val outerCircleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val outerCircleSize by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val staticOuterCircleSize = 1f
    val dotColor = Color.Magenta

    Box(
        modifier = Modifier
            .size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val maxRadius = size.minDimension / 2
            val circlePadding = 5f
            val circleRadii = listOf(1.0f, 0.8f, 0.6f)

            circleRadii.forEach { scale ->
                val alpha = if (isListening) {
                    outerCircleAlpha
                } else {
                    when (scale) {
                        1.0f -> 0.3f
                        0.8f -> 0.5f
                        0.6f -> 0.7f
                        else -> 1f
                    }
                }

                drawCircle(
                    color = dotColor.copy(alpha = alpha),
                    radius = maxRadius * scale * (if (isListening) outerCircleSize else staticOuterCircleSize) - circlePadding,
                    style = Stroke(width = 5f)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .size(80.dp)
                .clickable {
                    if (isListening) {
                        speechRecognizer.stopListening()
                    } else {
                        if (isSpeaking) {
                            tts.stop()
                        }
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            speechRecognizer.startListening(speechRecognizerIntent)
                        } else {
                            onPermissionDenied()
                        }
                    }
                },
            onDraw = {
                val maxRadius = size.minDimension / 2
                drawCircle(
                    color = Color.White,
                    radius = maxRadius
                )
            }
        )

        Image(
            painter = painterResource(id = R.drawable.microphone_1),
            contentDescription = "Microphone Icon",
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit
        )
    }
}

private fun getErrorMessage(errorCode: Int): String {
    return when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
        SpeechRecognizer.ERROR_CLIENT -> "Client-side error."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions."
        SpeechRecognizer.ERROR_NETWORK -> "Network error."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy."
        SpeechRecognizer.ERROR_SERVER -> "Server error."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input."
        else -> "Unknown error."
    }
}
