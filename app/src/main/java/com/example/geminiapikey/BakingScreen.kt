package com.example.geminiapikey

import UiState
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay

@Composable
fun BakingScreen(
    context: Context,
    bakingViewModel: BakingViewModel = viewModel()
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    val uiState by bakingViewModel.uiState.collectAsState()
    var displayedText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(getColor(context, R.color.splash_background)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.icons),
                contentDescription = "Go Back",
                modifier = Modifier
                    .clickable {
                        if (context is ComponentActivity) {
                            context.onBackPressedDispatcher.onBackPressed()
                        }
                    }
                    .padding(8.dp)
                    .requiredSize(32.dp)
            )
            Spacer(modifier = Modifier.weight(0.8f))
            Image(
                painter = painterResource(id = R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_1),
                contentDescription = "App Icon"
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_copy__component_additional_icons),
                    contentDescription = "Copy",
                    modifier = Modifier
                        .clickable {
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData =
                                ClipData.newPlainText("Copied Text", displayedText)
                            clipboardManager.setPrimaryClip(clipData)
                        }
                        .padding(8.dp)
                        .requiredSize(32.dp)
                )
                Image(
                    painter = painterResource(R.drawable.icon_share_alt__component_additional_icons),
                    contentDescription = "Share",
                    modifier = Modifier
                        .clickable {
                            val boldPrompt = "**${prompt.trim()}**"
                            val shareText = "$boldPrompt\n\nResponse: $displayedText"

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Share Chat"
                                )
                            )
                        }
                        .padding(8.dp)
                        .requiredSize(32.dp)
                )
            }

            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is UiState.Streaming -> {
                    val fullText = (uiState as UiState.Streaming).partialText
                    TypingEffectText(
                        fullText,
                        onTypingComplete = { displayedText = fullText })
                }

                is UiState.Success -> {
                    displayedText = (uiState as UiState.Success).outputText
                    TypingEffectText(displayedText)
                }

                is UiState.Error -> {
                    Text(
                        text = (uiState as UiState.Error).errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> Unit
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = {
                    Text(
                        "Ask your query ...",
                        color = Color.Gray,
                        style = TextStyle(fontStyle = FontStyle.Italic)
                    )
                },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            IconButton(
                onClick = {
                    bakingViewModel.sendPrompt(null, prompt)
                    prompt = ""
                },
                modifier = Modifier
                    .padding(end = 8.dp) // Add padding to separate the button from the edge
                    .size(36.dp) // Define the overall size of the button
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize() // Ensure the circle fully fits within the IconButton
                        .background(
                            color = Color(0xFF1E1E1E), // Orange background for contrast with the white icon
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center // Center the icon inside the circle
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.send), // Replace with your send icon resource
                        contentDescription = "Send",
                        tint = Color.White, // White icon to match your design
                        modifier = Modifier
                            .size(24.dp) // Adjust icon size
                    )
                }
            }
        }

        BannerAdView(
            context = context,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            adUnitIdValue = "ca-app-pub-3940256099942544/6300978111"
        )
    }
}

@Composable
fun TypingEffectText(
    fullText: String,
    typingSpeed: Long = 10L,
    onTypingComplete: (() -> Unit)? = null
) {
    var animatedText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(fullText) {
        animatedText = ""
        for (char in fullText) {
            animatedText += char
            delay(typingSpeed)
        }
        onTypingComplete?.invoke()
    }

    val annotatedText = buildAnnotatedString {
        val regex = Regex("\\*\\*(.*?)\\*\\*")
        var lastIndex = 0

        regex.findAll(animatedText).forEach { matchResult ->
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1

            append(animatedText.substring(lastIndex, startIndex))

            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(matchResult.groupValues[1])
            pop()

            lastIndex = endIndex
        }

        if (lastIndex < animatedText.length) {
            append(animatedText.substring(lastIndex))
        }
    }

    Text(
        text = annotatedText,
        color = Color.White,
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
            .fillMaxSize()
    )

}

@Composable
fun BannerAdView(context: Context, modifier: Modifier, adUnitIdValue: String){
    AndroidView(
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = adUnitIdValue
                loadAd(
                    AdRequest.Builder()
                        .build()
                )
            }
        },
        modifier = modifier
    )
}

