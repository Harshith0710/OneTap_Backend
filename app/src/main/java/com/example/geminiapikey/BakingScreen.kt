package com.example.geminiapikey

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    val promptResponseList = remember { mutableStateListOf<Pair<String, String>>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(getColor(context, R.color.splash_background)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Section
            Header(context)

            // Content Section with scrolling
            Box(
                modifier = Modifier
                    .weight(1f) // Allocate remaining space
                    .fillMaxWidth()
            ) {
                ContentSection(context, uiState, promptResponseList)
            }
            Spacer(modifier = Modifier.height(120.dp))
        }

        // Fixed Input and Ad Section at the Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()// Background for input and ads
        ) {
            // Input Section
            InputSection(
                prompt = prompt,
                onPromptChange = { prompt = it },
                onSendClick = {
                    bakingViewModel.sendPrompt(null, prompt)
                    promptResponseList.add(Pair(prompt, "")) // Add the prompt to the list
                    prompt = ""
                }
            )

            // Banner Ad Section
            BannerAdView(
                context = context,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                adUnitIdValue = "ca-app-pub-3940256099942544/6300978111"
            )
        }
    }
}

@Composable
fun ContentSection(
    context: Context,
    uiState: UiState,
    promptResponseList: MutableList<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()) // Enable scrolling
    ) {
        promptResponseList.forEachIndexed { _, pair ->
            val currentPrompt = pair.first
            val currentResponse = pair.second

            // Display the prompt and response unit
            PromptResponseUnit(
                prompt = currentPrompt,
                response = currentResponse,
                onCopy = {
                    val clipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData =
                        ClipData.newPlainText("Copied Text", currentResponse)
                    clipboardManager.setPrimaryClip(clipData)
                },
                onShare = {
                    val shareText = "$currentPrompt\n\nResponse: $currentResponse"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(
                        Intent.createChooser(shareIntent, "Share Chat")
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp)) // Add space between units
        }

        // Handle the UI state (loading, streaming, success, error)
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is UiState.Streaming -> {
                val fullText = uiState.partialText
                promptResponseList.lastOrNull()?.let {
                    promptResponseList[promptResponseList.lastIndex] = it.copy(second = fullText)
                }
            }

            is UiState.Success -> {
                promptResponseList.lastOrNull()?.let {
                    promptResponseList[promptResponseList.lastIndex] = it.copy(second = uiState.outputText)
                }
            }

            is UiState.Error -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> Unit
        }
    }
}

@Composable
fun PromptResponseUnit(
    prompt: String,
    response: String,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    var typingComplete by remember { mutableStateOf(false) } // Track if typing is complete

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ellipse_78),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = prompt,
                color = Color.White,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )
        }

        // White line between prompt and response
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Optional padding for better spacing
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_2),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(36.dp)
            )

            // Right-aligned action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Space between buttons
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    icon = R.drawable.icon_copy__component_additional_icons,
                    contentDescription = "Copy",
                    onClick = onCopy
                )
                ActionButton(
                    icon = R.drawable.icon_share_alt__component_additional_icons,
                    contentDescription = "Share",
                    onClick = onShare
                )
            }
        }

        TypingEffectText(
            fullText = response,
            typingSpeed = 30L,
            onTypingComplete = {
                typingComplete = true // Mark typing as complete
            }
        )

        // Only show the second horizontal line if typing is complete
        if (typingComplete) {
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ActionButton(icon: Int, contentDescription: String, onClick: () -> Unit) {
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
            .requiredSize(16.dp)
    )
}

@Composable
fun InputSection(prompt: String, onPromptChange: (String) -> Unit, onSendClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = prompt,
            onValueChange = onPromptChange,
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
            onClick = onSendClick,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF1E1E1E),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TypingEffectText(
    fullText: String,
    typingSpeed: Long = 30L,
    onTypingComplete: (() -> Unit)? = null
) {
    var animatedText by remember { mutableStateOf("") }

    LaunchedEffect(fullText) {
        animatedText = ""
        for (char in fullText) {
            animatedText += char
            delay(typingSpeed)
        }
        onTypingComplete?.invoke()
    }

    val annotatedText = buildAnnotatedString {
        val regex = Regex("\\*\\*(.*?)\\*\\*") // Matches text within double asterisks
        var lastIndex = 0

        regex.findAll(animatedText).forEach { matchResult ->
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1

            // Add plain text before the bold section
            append(animatedText.substring(lastIndex, startIndex))

            // Add bold text
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(matchResult.groupValues[1]) // Extract bold content
            pop()

            lastIndex = endIndex
        }

        // Add remaining plain text
        if (lastIndex < animatedText.length) {
            append(animatedText.substring(lastIndex))
        }
    }

    Text(
        text = annotatedText,
        color = Color.White,
        style = TextStyle(fontSize = 16.sp),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun Header(context: Context) {
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
}

@Composable
fun BannerAdView(context: Context, modifier: Modifier, adUnitIdValue: String) {
    AndroidView(
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = adUnitIdValue
                loadAd(
                    AdRequest.Builder().build()
                )
            }
        },
        modifier = modifier
    )
}
