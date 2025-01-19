package com.example.geminiapikey

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlin.math.abs
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
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionState.value = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permission is required to use the microphone.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) { launcher.launch(permission) }

    if (permissionState.value) { onGranted() }
}

fun getLineCount(text: String, maxLineLength: Int): Int = if (text.isEmpty()) 0 else (text.length / maxLineLength) + 1

@Composable
fun SpeakScreen(viewModel: BakingViewModel = viewModel()) {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var displayedText by remember { mutableStateOf("I’m having anxiety about my career, can you help me dealing with it ? ") }
    var partialText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var amplitude by remember { mutableFloatStateOf(0.1f) }
    var isSpeaking by remember { mutableStateOf(false) }
    var currentTTSChunk by remember { mutableStateOf("") }
    val maxLineLength = 20

    val lineCount = getLineCount(displayedText, maxLineLength)

    val textAlpha by animateFloatAsState(targetValue = 1f - (lineCount * 0.1f).coerceIn(0f, 1f),
        label = ""
    )

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(context, "TTS Initialization Failed", Toast.LENGTH_SHORT).show()
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { isSpeaking = true }
                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    currentTTSChunk = ""
                    displayedText = ""
                }
                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    currentTTSChunk = ""
                    if (displayedText != spokenText) { displayedText = "" }
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
        override fun onReadyForSpeech(params: Bundle?) { isListening = true }
        override fun onBeginningOfSpeech() { isListening = true; displayedText = "" }
        override fun onRmsChanged(p0: Float) {}
        override fun onBufferReceived(p0: ByteArray?) {}
        override fun onEndOfSpeech() { isListening = false }
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
        Image(painter = painterResource(R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_2), contentDescription = null, modifier = Modifier.size(80.dp))
        AnimatedLines(amplitude = if (isSpeaking) amplitude else 1f, isSpeaking = isSpeaking)
        Text(
            text = if (isListening) partialText else displayedText,
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            fontSize = 28.sp,
            maxLines = 5,
            textAlign = TextAlign.Center
        )
        Text(text = if (isSpeaking) "[ $currentTTSChunk ]" else "", color = Color.White, modifier = Modifier.padding(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clickable { context.startActivity(Intent(context, BakingActivity::class.java)) },
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(R.drawable.ellipse_84), contentDescription = null, modifier = Modifier.fillMaxSize())
                Image(painter = painterResource(R.drawable.keyboard_10_1), contentDescription = null, modifier = Modifier.size(25.dp))
            }
            CircularPulseAnimation(
                isListening = isListening,
                isSpeaking = isSpeaking,
                speechRecognizer = speechRecognizer,
                speechRecognizerIntent = speechRecognizerIntent,
                tts = tts,
                onPermissionDenied = { Toast.makeText(context, "Permission denied. Please enable microphone access.", Toast.LENGTH_SHORT).show() },
                onTtsStop = { isSpeaking = false }
            )
            Box(
                modifier = Modifier.size(50.dp)
                    .clickable { if (context is ComponentActivity) { context.onBackPressedDispatcher.onBackPressed() } },
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(R.drawable.ellipse_85), contentDescription = "", modifier = Modifier.fillMaxSize())
                Image(painter = painterResource(R.drawable.fi_rr_cross_small_1_1), contentDescription = "", modifier = Modifier.size(25.dp))
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
            LaunchedEffect(state) { Toast.makeText(context, state.errorMessage, Toast.LENGTH_SHORT).show() }
        }
        else -> {}
    }
}

@Composable
fun AnimatedLines(amplitude: Float, isSpeaking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val lineCount = 9
    val middleLineIndex = lineCount / 2
    val staticAmplitudes = listOf(0.3f, 0.2f, 0.3f, 0.5f, 0.55f, 0.5f, 0.3f, 0.2f, 0.3f)
    val lineHeights = if (isSpeaking) {
        List(lineCount) {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(animation = tween(durationMillis = Random.nextInt(300, 700), easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                label = ""
            )
        }
    } else staticAmplitudes.map { mutableFloatStateOf(it) }

    Canvas(modifier = Modifier.size(200.dp).padding(16.dp)) {
        val baseLineWidth = size.width / (lineCount * 5f)
        val spaceBetween = size.width / (lineCount)

        lineHeights.forEachIndexed { index, animation ->
            val height by animation
            var dynamicHeight = height * amplitude * size.height / 2
            if (index == middleLineIndex || index == 0 || index == lineCount - 1) { dynamicHeight *= 1.5f }
            val lineWidth = if (index == middleLineIndex) baseLineWidth * 1.2f else baseLineWidth * (1 - 0.1f * abs(middleLineIndex - index))

            val greenValue = (60 + index * (41 / (lineCount - 1))).coerceIn(0, 255)
            val blueValue = (166 - index * (62 / (lineCount - 1))).coerceIn(0, 255)

            drawLine(
                color = Color(255, greenValue, blueValue),
                start = Offset(x = spaceBetween * (index + 1) + lineWidth / 2, y = size.height / 2 - dynamicHeight),
                end = Offset(x = spaceBetween * (index + 1) + lineWidth / 2, y = size.height / 2 + dynamicHeight),
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
    onPermissionDenied: () -> Unit,
    onTtsStop: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val outerCircleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = ""
    )

    val outerCircleSize by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = ""
    )

    val staticOuterCircleSize = 1f
    val dotColor = Color(255,60,159)

    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val maxRadius = size.minDimension / 2
            val circlePadding = 5f
            val circleRadii = listOf(1.0f, 0.8f, 0.6f)

            circleRadii.forEach { scale ->
                val alpha = if (isListening) outerCircleAlpha else when (scale) {
                    1.0f -> 0.1f
                    0.8f -> 0.3f
                    0.6f -> 0.5f
                    else -> 1f
                }

                drawCircle(
                    color = dotColor.copy(alpha = alpha),
                    radius = maxRadius * scale * (if (isListening) outerCircleSize else staticOuterCircleSize) - circlePadding,
                    style = Stroke(width = 5f)
                )
            }
        }

        Canvas(modifier = Modifier.size(80.dp).clickable {
            if (isListening) {
                speechRecognizer.stopListening()
            } else {
                if (isSpeaking) {
                    tts.stop()
                    onTtsStop()
                }
                val hasPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    speechRecognizer.startListening(speechRecognizerIntent)
                } else {
                    onPermissionDenied()
                }
            }
        }) {
            val maxRadius = size.minDimension / 2
            drawCircle(color = Color.White, radius = maxRadius)
        }

        Image(painter = painterResource(id = R.drawable.microphone_1), contentDescription = "Microphone Icon", modifier = Modifier.size(40.dp), contentScale = ContentScale.Fit)
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
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input."
        else -> "Unknown error."
    }
}
