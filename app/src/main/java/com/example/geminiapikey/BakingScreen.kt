package com.example.geminiapikey

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.withStyle
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

@Composable
fun BakingScreen(
    context: Context,
    image: Bitmap?,
    conversations: List<String> = emptyList(),
    bakingViewModel: BakingViewModel = viewModel(),
    prePrompt: String
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf(image) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val promptResponseList = remember { mutableStateListOf<Triple<String, String, Bitmap?>>() }
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var isSentOrRefreshed by remember { mutableStateOf(false) }
    val isResponseUpdating = remember { mutableStateOf(false) }
    var isRefreshDisabled by remember { mutableStateOf(false) }
    var isStopButtonVisible by remember { mutableStateOf(false) }
    var isStopButtonEnabled by remember { mutableStateOf(false) }
    val isLoading = uiState is UiState.Loading
    val stopTypingMap = remember { mutableStateMapOf<Int, Boolean>() }
    val displayedTextMap = remember { mutableStateMapOf<Int, String>() }
    var refreshingIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(image) {
        if (image != null) {
            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(prePrompt, conversations) {
        if (prePrompt.isNotEmpty()) {
            promptResponseList.add(Triple(prePrompt, "", null))
            bakingViewModel.currentIndex = 0
            bakingViewModel.sendPrompt(null, prePrompt)
        }

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
                isRefreshDisabled = false
                isStopButtonVisible = false // Reset to "Send" button
                isStopButtonEnabled = false // Disable "Stop" button
                val response = (uiState as UiState.Success).outputText
                val index = bakingViewModel.currentIndex
                if (index != null) {
                    promptResponseList[index] = Triple(
                        promptResponseList[index].first,
                        response,
                        promptResponseList[index].third
                    )
                    displayedTextMap[index] = ""
                    stopTypingMap[index] = false
                    bakingViewModel.currentIndex = null
                }
                else {
                    promptResponseList.add(Triple(prompt, response, selectedImage))
                    selectedImage = null
                }
                isResponseUpdating.value = false
                prompt = ""
                refreshingIndex = null
            }
            is UiState.Error -> {
                isRefreshDisabled = false
                isStopButtonVisible = false // Revert to "Send" button on error
                isStopButtonEnabled = false // Disable "Stop" button
                isSentOrRefreshed =false
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
                    onCopy = {
                        val clipboardManager =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("Copied Text", it)
                        clipboardManager.setPrimaryClip(clipData)
                    },
                    onShare = { prompt, response ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Prompt: $prompt\nResponse: $response")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    },
                    onRefresh = { refreshIndex ->
                        refreshingIndex = refreshIndex
                        isSentOrRefreshed = true
                        isResponseUpdating.value = true
                        val (prompt, _, image) = promptResponseList[refreshIndex]
                        bakingViewModel.refreshPrompt(refreshIndex, prompt, image)
                    },
                    user = user,
                    predefinedConversationsCount = conversations.size,
                    isResponseUpdating = isResponseUpdating,
                    stopTypingMap = stopTypingMap,
                    displayedTextMap = displayedTextMap,
                    onTypingStarted = {
                        isStopButtonEnabled = true
                    },
                    isSentOrRefreshed = isSentOrRefreshed,
                    onComplete = {
                        isSentOrRefreshed = false
                    }
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
                        isSentOrRefreshed = true
                        isStopButtonVisible = true // Show "Stop" button
                        isStopButtonEnabled = false // Disable "Stop" button during loading
                        isRefreshDisabled = true // Disable refresh button
                        bakingViewModel.sendPrompt(selectedImage, prompt)
                    }
                },
                onImageSelectClick = { imageLauncher.launch("image/*") },
                onStopClick = {
                    isSentOrRefreshed = false
                    isStopButtonVisible = false // Revert to "Send" button
                    isStopButtonEnabled = false // Disable "Stop" button
                    isRefreshDisabled = false
                    val index = refreshingIndex ?: (promptResponseList.size - 1)
                    val currentDisplayedText = displayedTextMap[index] ?: ""
                    promptResponseList[index] = Triple(
                        promptResponseList[index].first,
                        currentDisplayedText,
                        promptResponseList[index].third
                    )
                    stopTypingMap[index] = true
                    refreshingIndex = null
                },
                isStopButtonEnabled = isStopButtonEnabled,
                isLoading = isLoading,
                isSentOrRefreshed = isSentOrRefreshed
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
    user: FirebaseUser?,
    predefinedConversationsCount: Int,
    isResponseUpdating: MutableState<Boolean>,
    stopTypingMap: SnapshotStateMap<Int, Boolean>,
    displayedTextMap: SnapshotStateMap<Int, String>,
    onTypingStarted: () -> Unit,
    isSentOrRefreshed: Boolean,
    onComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(
            items = promptResponseList,
            key = { index, triple -> "$index-${triple.first.hashCode()}" }
        ) { index, triple ->
            val isFromConversations = index < predefinedConversationsCount
            val shouldShowFullText = rememberSaveable { mutableStateOf(isFromConversations) }

            PromptResponseUnit(
                prompt = triple.first,
                response = triple.second,
                onCopy = { onCopy(triple.second) },
                onShare = { onShare(triple.first, triple.second) },
                onRefresh = {
                    shouldShowFullText.value = false
                    onRefresh(index)
                },
                uploadedImage = triple.third,
                showFullText = shouldShowFullText.value,
                onShowFullText = {
                    shouldShowFullText.value = true
                    onComplete()
                },
                user = user,
                isFromConversations = isFromConversations,
                isResponseUpdating = isResponseUpdating,
                stopTyping = stopTypingMap[index] ?: false,
                displayedText = displayedTextMap[index] ?: "",
                onDisplayedTextUpdate = { newText -> displayedTextMap[index] = newText },
                onTypingStarted = onTypingStarted,
                isSentOrRefreshed = isSentOrRefreshed
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState is UiState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        else if (uiState is UiState.Error) {
            item {
                Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun TypingEffectText(
    fullText: String,
    typingSpeed: Long = 30L,
    onComplete: () -> Unit,
    stopTyping: Boolean,
    displayedText: String,
    onDisplayedTextUpdate: (String) -> Unit,
    onTypingStarted: () -> Unit
) {
    LaunchedEffect(fullText, stopTyping) {
        if (stopTyping) {
            return@LaunchedEffect
        }

        // Notify that typing has started
        onTypingStarted()

        // Reset displayedText when fullText changes
        onDisplayedTextUpdate("")
        for (i in fullText.indices) {
            onDisplayedTextUpdate(fullText.substring(0, i + 1))
            delay(typingSpeed)
        }
        onComplete()
    }

    // Process the displayedText to replace **Heading** with bold text
    val annotatedString = buildAnnotatedString {
        val regex = Regex("\\*\\*(.*?)\\*\\*") // Regex to match **Heading**
        var currentIndex = 0

        // Find all matches of **Heading**
        regex.findAll(displayedText).forEach { matchResult ->
            // Append text before the match
            append(displayedText.substring(currentIndex, matchResult.range.first))

            // Append the bold text (without asterisks)
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(matchResult.groupValues[1]) // Group 1 is the text inside **
            }

            // Update currentIndex to the end of the match
            currentIndex = matchResult.range.last + 1
        }

        // Append remaining text after the last match
        if (currentIndex < displayedText.length) {
            append(displayedText.substring(currentIndex))
        }
    }

    Text(
        text = annotatedString,
        style = TextStyle(fontSize = 16.sp, color = Color.LightGray),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun PromptResponseUnit(
    prompt: String,
    response: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRefresh: () -> Unit,
    uploadedImage: Bitmap?,
    showFullText: Boolean,
    onShowFullText: () -> Unit,
    user: FirebaseUser?,
    isFromConversations: Boolean,
    isResponseUpdating: MutableState<Boolean>,
    stopTyping: Boolean,
    displayedText: String,
    onDisplayedTextUpdate: (String) -> Unit,
    onTypingStarted: () -> Unit,
    isSentOrRefreshed: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Display user profile picture and email
        val photoUrl = user?.photoUrl
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (photoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape) // Ensure it's circular
                )
            } else {
                // Fallback in case photoUrl is null
                Image(
                    painter = painterResource(id = R.drawable.icons8_test_account_100),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = prompt,
                color = Color.White,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    icon = R.drawable.icon_copy__component_additional_icons,
                    contentDescription = "Copy",
                    onClick = onCopy,
                    isSentOrRefreshed = true
                )
                ActionButton(
                    icon = R.drawable.icon_share_alt__component_additional_icons,
                    contentDescription = "Share",
                    onClick = onShare,
                    isSentOrRefreshed = true
                )
                ActionButton(
                    icon = R.drawable.icons8_refresh_50,
                    contentDescription = "Refresh",
                    onClick = onRefresh,
                    isSentOrRefreshed = isSentOrRefreshed
                )
            }
        }

        if (showFullText || isFromConversations || isResponseUpdating.value) {
            // Display the full text with bold formatting
            val annotatedString = buildAnnotatedString {
                val regex = Regex("\\*\\*(.*?)\\*\\*")
                var currentIndex = 0

                regex.findAll(response).forEach { matchResult ->
                    append(response.substring(currentIndex, matchResult.range.first))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchResult.groupValues[1])
                    }
                    currentIndex = matchResult.range.last + 1
                }

                if (currentIndex < response.length) {
                    append(response.substring(currentIndex))
                }
            }

            Text(
                text = annotatedString,
                style = TextStyle(fontSize = 16.sp, color = Color.LightGray),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        else {
            // Use TypingEffectText for the typing effect
            TypingEffectText(
                fullText = response,
                typingSpeed = 30L,
                onComplete = onShowFullText,
                stopTyping = stopTyping,
                displayedText = displayedText,
                onDisplayedTextUpdate = onDisplayedTextUpdate,
                onTypingStarted = onTypingStarted
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
fun InputSection(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageSelectClick: () -> Unit,
    onStopClick: () -> Unit,
    isStopButtonEnabled: Boolean,
    isLoading: Boolean,
    isSentOrRefreshed: Boolean
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

        if (isSentOrRefreshed) {
            IconButton(
                onClick = {
                    if (!isLoading) {
                        onStopClick() // Invoke the onStopClick lambda
                    }
                },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .alpha(if (isStopButtonEnabled) 1f else 0.5f) // Reduce opacity if disabled
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icons8_stop_50), // Stop icon
                    contentDescription = "Stop",
                    tint = Color.White
                )
            }
        }
        else {
            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
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
fun ActionButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit, // Add this parameter
    isSentOrRefreshed: Boolean
) {
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        modifier = Modifier
            .clickable { if(!isSentOrRefreshed) onClick() } // Use the enabled state
            .padding(8.dp)
            .requiredSize(16.dp)
    )
}