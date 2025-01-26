package com.example.geminiapikey

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BakingScreen(
    context: Context,
    image: Bitmap?,
    conversations: List<String> = emptyList(), // Add conversations parameter
    bakingViewModel: BakingViewModel = viewModel()
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Bitmap?>(image) }
    var stopTyping by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val promptResponseList = remember { mutableStateListOf<Triple<String, String, Bitmap?>>() }
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    context as? Activity

    // Initialize promptResponseList with conversations
    LaunchedEffect(conversations) {
        conversations.forEach { conversation ->
            val (prompt, response) = conversation.split("\n", limit = 2)
            promptResponseList.add(Triple(prompt, response, null))
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val bitmap = getBitmapFromUri(context.contentResolver, it)
                selectedImage = bitmap
                Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                val response = (uiState as UiState.Success).outputText
                val index = bakingViewModel.currentIndex
                if (index != null) {
                    promptResponseList[index] = Triple(
                        promptResponseList[index].first,
                        response,
                        promptResponseList[index].third
                    )
                    bakingViewModel.currentIndex = null
                } else {
                    // Add the prompt, response, and selectedImage to the list
                    promptResponseList.add(Triple(prompt, response, selectedImage))
                    selectedImage = null // Reset selectedImage after adding to the list
                }
                stopTyping = false
                isTyping = false // Reset isTyping after response
                prompt = "" // Clear the prompt text field
            }
            is UiState.Error -> {
                Toast.makeText(context, (uiState as UiState.Error).errorMessage, Toast.LENGTH_SHORT).show()
                isTyping = false
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(getColor(context, R.color.splash_background)))
            .windowInsetsPadding(WindowInsets.safeContent.union(WindowInsets.navigationBars))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(context)

            Box(
                modifier = Modifier
                    .width(0.42f * screenWidth)
                    .background(
                        shape = RoundedCornerShape(50),
                        color = Color.White
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "K.I.R.A",
                    color = Color.Black,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(5.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                ContentSection(
                    context = context,
                    uiState = uiState,
                    promptResponseList = promptResponseList,
                    stopTyping = stopTyping,
                    onCopy = {
                        val clipboardManager =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("Copied Text", "")
                        clipboardManager.setPrimaryClip(clipData)
                    },
                    onShare = { prompt, response ->
                        // Implement share functionality
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Prompt: $prompt\nResponse: $response")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    },
                    onRefresh = { refreshIndex ->
                        val (prompt, _, image) = promptResponseList[refreshIndex]
                        bakingViewModel.refreshPrompt(refreshIndex, prompt, image)
                    },
                    user = user,
                    predefinedConversationsCount = conversations.size // Pass the user object to ContentSection
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            InputSection(
                prompt = prompt,
                onPromptChange = { prompt = it },
                onSendClick = {
                    if (prompt.isNotEmpty()) {
                        stopTyping = false
                        isTyping = true
                        bakingViewModel.sendPrompt(selectedImage, prompt)
                        // Do not reset selectedImage here; it will be reset after the response is added to the list
                    }
                },
                onImageSelectClick = { imageLauncher.launch("image/*") },
                onStopClick = {
                    stopTyping = true
                    isTyping = false
                },
                isTyping = isTyping
            )

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
    promptResponseList: List<Triple<String, String, Bitmap?>>,
    onCopy: (String) -> Unit,
    onShare: (String, String) -> Unit,
    onRefresh: (Int) -> Unit,
    stopTyping: Boolean,
    user: FirebaseUser?, // Add user parameter
    predefinedConversationsCount: Int // Pass the count of predefined conversations
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(
            items = promptResponseList,
            key = { index, triple -> "$index-${triple.first.hashCode()}" } // Stable key
        ) { index, triple ->
            val isFromConversations = index < predefinedConversationsCount // Check if it's from predefined conversations
            val shouldShowFullText = rememberSaveable { mutableStateOf(isFromConversations) } // Show full text for predefined conversations

            PromptResponseUnit(
                prompt = triple.first,
                response = triple.second,
                onCopy = { onCopy(triple.second) },
                onShare = { onShare(triple.first, triple.second) },
                onRefresh = {
                    shouldShowFullText.value = false // Reset state on refresh
                    onRefresh(index)
                },
                uploadedImage = triple.third,
                stopTyping = stopTyping,
                showFullText = shouldShowFullText.value,
                onShowFullText = { shouldShowFullText.value = true },
                user = user, // Pass the user object to PromptResponseUnit
                isFromConversations = isFromConversations // Pass the flag
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState is UiState.Loading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
            }
        } else if (uiState is UiState.Error) {
            item {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
    else {
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }
}


@Composable
fun PromptResponseUnit(
    prompt: String,
    response: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRefresh: () -> Unit,
    uploadedImage: Bitmap?,
    stopTyping: Boolean,
    showFullText: Boolean,
    onShowFullText: () -> Unit,
    user: FirebaseUser?, // Add user parameter
    isFromConversations: Boolean // Add flag to check if it's from conversations
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Display user profile picture and email
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            user?.photoUrl?.let { photoUrl ->
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = prompt,
                color = Color.White,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        uploadedImage?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Uploaded Image",
                modifier = Modifier
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(top = 4.dp, bottom = 8.dp)
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                ActionButton(
                    icon = R.drawable.icons8_refresh_50, // Assuming you have a refresh icon
                    contentDescription = "Refresh",
                    onClick = onRefresh
                )
            }
        }

        // Show response text
        if (showFullText || isFromConversations) {
            Text(
                text = response,
                style = TextStyle(fontSize = 16.sp, color = Color.LightGray),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            TypingEffectText(
                fullText = response,
                typingSpeed = 30L,
                stopTyping = stopTyping,
                onComplete = onShowFullText
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
fun InputSection(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageSelectClick: () -> Unit,
    onStopClick: () -> Unit,
    isTyping: Boolean
) {
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
            onClick = onImageSelectClick,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(36.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_image_upload),
                contentDescription = "Select Image",
                tint = Color.White
            )
        }

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
                    painter = painterResource(R.drawable.send_ic),
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
    typingSpeed: Long = 30L, // Milliseconds between each character
    stopTyping: Boolean,
    onComplete: () -> Unit
) {
    var displayedText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(fullText, stopTyping) {
        if (stopTyping) {
            // Immediately show the full text if typing should stop
            displayedText = fullText
            onComplete()
        } else {
            displayedText = ""
            coroutineScope.launch {
                for (i in fullText.indices) {
                    displayedText = fullText.substring(0, i + 1)
                    delay(typingSpeed)
                }
                onComplete()
            }
        }
    }

    Text(
        text = displayedText,
        style = TextStyle(fontSize = 16.sp, color = Color.LightGray),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}


@Composable
fun Header(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
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
                .padding(start = 16.dp)
                .requiredSize(32.dp)
        )
        Spacer(modifier = Modifier.weight(0.6f))
        Image(
            painter = painterResource(id = R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_1),
            contentDescription = "App Icon",
            modifier = Modifier.size(120.dp)
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