package com.example.geminiapikey

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
fun SpeakScreen() {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var amplitude by remember { mutableFloatStateOf(0.1f) }

    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
        }

        override fun onBeginningOfSpeech() {
            isListening = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            amplitude = (rmsdB + 2).coerceAtLeast(0.1f) / 10
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            isListening = false
        }

        override fun onError(error: Int) {
            isListening = false
            Toast.makeText(context, "Error recognizing speech: $error", Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0) ?: ""
        }

        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = spokenText.ifEmpty { "Speak something!" },
            modifier = Modifier.padding(16.dp)
        )

        if (isListening) {
            AnimatedLines(amplitude = amplitude)
        }
        Button(onClick = {
            if (isListening) {
                speechRecognizer.stopListening()
            }
            else {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    speechRecognizer.startListening(speechRecognizerIntent)
                }
                else {
                    Toast.makeText(context, "Permission denied. Please enable microphone access.", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text(if (isListening) "Stop Listening" else "Start Listening")
        }
    }
}

@Composable
fun AnimatedLines(amplitude: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "Irregular Voice Lines")
    val lineCount = 10
    val randomAmplitudes = remember { List(lineCount) { Random.nextDouble(0.5, 1.0).toFloat() } }

    val lineHeights = randomAmplitudes.map { _ ->
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

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
    ) {
        val lineWidth = size.width / (lineCount * 2)
        val spaceBetween = size.width / lineCount

        lineHeights.forEachIndexed { index, animation ->
            val height by animation
            val dynamicHeight = height * amplitude * randomAmplitudes[index] * size.height / 2
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
                strokeWidth = lineWidth
            )
        }
    }
}