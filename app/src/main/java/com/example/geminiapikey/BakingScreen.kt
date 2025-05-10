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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
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

data class PromptResponse(var prompt: String, val response: String, val image: Bitmap?)

@Composable
fun BakingScreen(
    context: Context,
    image: Bitmap?,
    conversations: List<String> = emptyList(),
    bakingViewModel: BakingViewModel = viewModel(),
    prePrompt: String
) {
    var temp by remember { mutableStateOf("") }
    var prompt by rememberSaveable { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf(image) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val promptResponseList = remember { mutableStateListOf<PromptResponse>() }
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
            promptResponseList.add(PromptResponse(prePrompt, "", null))
            bakingViewModel.currentIndex = 0
            bakingViewModel.sendPrompt(null, prePrompt)
        }

        conversations.forEach { conversation ->
            val (prompt, response) = conversation.split("\n", limit = 2)
            promptResponseList.add(PromptResponse(prompt, response, null))
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
                isStopButtonVisible = false
                isStopButtonEnabled = false
                val response = (uiState as UiState.Success).outputText
                val index = bakingViewModel.currentIndex
                if (index != null) {
                    promptResponseList[index] = promptResponseList[index].copy(response = response)
                    displayedTextMap[index] = ""
                    stopTypingMap[index] = false
                    bakingViewModel.currentIndex = null
                } else {
                    promptResponseList.add(PromptResponse(temp, response, selectedImage))
                    selectedImage = null
                }
                isResponseUpdating.value = false
                refreshingIndex = null
            }
            is UiState.Error -> {
                isRefreshDisabled = false
                isStopButtonVisible = false
                isStopButtonEnabled = false
                isSentOrRefreshed = false
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
                            putExtra(Intent.EXTRA_TEXT, "You: $prompt\nKIRA: $response")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    },
                    onRefresh = { refreshIndex ->
                        refreshingIndex = refreshIndex
                        isSentOrRefreshed = true
                        isResponseUpdating.value = true
                        isStopButtonVisible = true
                        isStopButtonEnabled = true
                        stopTypingMap[refreshIndex] = false // Start typing
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
                        temp = prompt
                        prompt = ""
                        isSentOrRefreshed = true
                        isStopButtonVisible = true
                        isStopButtonEnabled = false
                        isRefreshDisabled = true
                        bakingViewModel.sendPrompt(selectedImage, temp)
                    }
                },
                onImageSelectClick = { imageLauncher.launch("image/*") },
                onStopClick = {
                    isSentOrRefreshed = false
                    isStopButtonVisible = false
                    isStopButtonEnabled = false
                    isRefreshDisabled = false
                    val index = refreshingIndex ?: (promptResponseList.size - 1)
                    val currentDisplayedText = displayedTextMap[index] ?: ""
                    promptResponseList[index] = promptResponseList[index].copy(response = currentDisplayedText)
                    for (i in 0 until promptResponseList.size) {
                        stopTypingMap[i] = true
                    }
                    refreshingIndex = null
                },
                image = selectedImage,
                onRemoveImage = { selectedImage = null },
                isStopButtonEnabled = isStopButtonEnabled,
                isLoading = isLoading,
                isSentOrRefreshed = isSentOrRefreshed
            )

            BannerAdView(
                context = context,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                adUnitIdValue = "ca-app-pub-4239057622725171/6680582081"
            )
        }
    }
}
@Composable
fun ContentSection(
    uiState: UiState,
    promptResponseList: List<PromptResponse>,
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
            key = { index, promptResponse -> "$index-${promptResponse.prompt.hashCode()}" }
        ) { index, promptResponse ->
            val isFromConversations = index < predefinedConversationsCount
            val shouldShowFullText = rememberSaveable { mutableStateOf(isFromConversations) }

            PromptResponseUnit(
                index = index,
                prompt = promptResponse.prompt,
                response = promptResponse.response,
                onCopy = { onCopy(promptResponse.response) },
                onShare = { onShare(promptResponse.prompt, promptResponse.response) },
                onRefresh = {
                    shouldShowFullText.value = false
                    onRefresh(index)
                },
                uploadedImage = promptResponse.image,
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
                isSentOrRefreshed = isSentOrRefreshed,
                onEditPrompt = { newPrompt ->
                    promptResponse.prompt = newPrompt
                    shouldShowFullText.value = false
                    onRefresh(index)
                }
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
    onTypingStarted: () -> Unit,
    index: Int
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var isCompleted by remember { mutableStateOf(false) }
    val typingKey = remember { mutableIntStateOf(index) }

    // Start the typing effect
    LaunchedEffect(typingKey.intValue, stopTyping) {
        if (stopTyping || isCompleted) return@LaunchedEffect

        onTypingStarted()
        if (displayedText.isEmpty()) {
            onDisplayedTextUpdate("")
        }
        for (i in displayedText.length until fullText.length) {
            onDisplayedTextUpdate(fullText.substring(0, i + 1))
            delay(typingSpeed)
        }
        isCompleted = true
        onComplete()
    }

    // Regex for bold text and code blocks
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    val codeRegex = Regex("```(.*?)\\n([\\s\\S]*?)```", RegexOption.DOT_MATCHES_ALL)

    var currentIndex = 0
    val matches = (boldRegex.findAll(displayedText) + codeRegex.findAll(displayedText)).sortedBy { it.range.first }
    val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val annotatedString = buildAnnotatedString {
        matches.forEach { matchResult ->
            val textBefore = displayedText.substring(currentIndex, matchResult.range.first)
            append(textBefore)

            when {
                codeRegex.matches(matchResult.value) -> { // Code block
                    val language = matchResult.groupValues[1].trim()
                    val code = matchResult.groupValues[2].trim()
                    val placeholder = "CODE_BLOCK_${matchResult.range.first}"  // Ensure unique key
                    appendInlineContent(placeholder, "[CODE BLOCK]")  // Reference the placeholder

                    // Estimate the height based on the number of lines in the code
                    val estimatedHeight = (code.lines().size + 1) * 21.sp  // Adjust line height based on the number of lines

                    // Store the inline content for this code block
                    inlineContentMap[placeholder] = InlineTextContent(
                        Placeholder(
                            width = (screenWidth * 0.8).sp,  // Use TextUnit.Infinity to take up maximum space
                            height = estimatedHeight,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextTop
                        )
                    ) {
                        CodeBlock(language, code) {
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("Code", code)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                boldRegex.matches(matchResult.value) -> { // Bold text
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchResult.groupValues[1])
                    }
                }
            }
            currentIndex = matchResult.range.last + 1
        }

        if (currentIndex < displayedText.length) {
            append(displayedText.substring(currentIndex))
        }
    }

    BasicText(
        text = annotatedString,
        style = TextStyle(fontSize = 16.sp, color = Color.LightGray),
        inlineContent = inlineContentMap,  // Pass the inline content map here
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}



@Composable
fun CodeBlock(language: String, code: String, onCopy: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp, max = 250.dp) // Fixed height with scroll
            .verticalScroll(scrollState)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifEmpty { "Code" },
                style = TextStyle(fontSize = 14.sp, color = Color.Black),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            IconButton(
                onClick = { onCopy() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_copy__component_additional_icons),
                    contentDescription = "Copy Code",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = code,
            style = TextStyle(fontSize = 14.sp, color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PromptResponseUnit(
    index: Int,
    prompt: String,
    response: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRefresh: () -> Unit,
    onEditPrompt: (String) -> Unit,
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
    var isEditing by remember { mutableStateOf(false) }
    var editedPrompt by rememberSaveable { mutableStateOf(prompt) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val context = LocalContext.current
        val screenWidth = LocalConfiguration.current.screenWidthDp

        // Profile and prompt section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = if (user?.photoUrl != null) rememberAsyncImagePainter(user.photoUrl)
                else painterResource(id = R.drawable.icons8_test_account_100),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (isEditing) {
                TextField(
                    value = editedPrompt,
                    onValueChange = { editedPrompt = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Black)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color(0xFF333333),
                        cursorColor = Color.Cyan,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text("Type your prompt...", color = Color.Gray)
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                IconButton(onClick = {
                    isEditing = false
                    onEditPrompt(editedPrompt)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.send_ic),
                        contentDescription = "Save",
                        tint = Color.White
                    )
                }
            } else {
                Text(
                    text = prompt,
                    color = Color.White,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                ActionButton(icon = R.drawable.icons8_edit_128, contentDescription = "Edit", onClick = { isEditing = true }, isSentOrRefreshed = isSentOrRefreshed)
            }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ActionButton(icon = R.drawable.icon_copy__component_additional_icons, contentDescription = "Copy", onClick = onCopy, isSentOrRefreshed = true)
                ActionButton(icon = R.drawable.icon_share_alt__component_additional_icons, contentDescription = "Share", onClick = onShare, isSentOrRefreshed = true)
                ActionButton(icon = R.drawable.icons8_refresh_50, contentDescription = "Refresh", onClick = onRefresh, isSentOrRefreshed = isSentOrRefreshed)
            }
        }

        if (showFullText || isFromConversations || isResponseUpdating.value) {
            val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
            val codeRegex = Regex("```(.*?)\\n([\\s\\S]*?)```", RegexOption.DOT_MATCHES_ALL)

            var currentIndex = 0
            val matches = (boldRegex.findAll(response) + codeRegex.findAll(response)).sortedBy { it.range.first }
            val inlineContentMap = mutableMapOf<String, InlineTextContent>()

            val annotatedString = buildAnnotatedString {
                matches.forEach { matchResult ->
                    val textBefore = response.substring(currentIndex, matchResult.range.first)
                    append(textBefore)

                    when {
                        codeRegex.matches(matchResult.value) -> {
                            val language = matchResult.groupValues[1].trim()
                            val code = matchResult.groupValues[2].trim()
                            val placeholder = "CODE_BLOCK_${matchResult.range.first}"
                            appendInlineContent(placeholder, "[CODE BLOCK]")
                            val density = LocalDensity.current
                            val fixedHeight = 250.dp
                            val fixedWidth = (screenWidth * 0.9).dp
                            val fixedWidthSp: TextUnit = with(density) { fixedWidth.toSp() }
                            val fixedHeightSp: TextUnit = with(density) { fixedHeight.toSp() }

                            inlineContentMap[placeholder] = InlineTextContent(
                                Placeholder(
                                    width = fixedWidthSp,
                                    height = fixedHeightSp,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.Top
                                )
                            ) {
                                CodeBlock(language, code, onCopy = {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("Code", code)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                })
                            }
                        }
                        boldRegex.matches(matchResult.value) -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(matchResult.groupValues[1])
                            }
                        }
                    }
                    currentIndex = matchResult.range.last + 1
                }
                if (currentIndex < response.length) {
                    append(response.substring(currentIndex))
                }
            }

            BasicText(
                text = annotatedString,
                style = TextStyle(fontSize = 16.sp, color = Color.LightGray),
                inlineContent = inlineContentMap,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            TypingEffectText(
                fullText = response,
                typingSpeed = 30L,
                onComplete = onShowFullText,
                stopTyping = stopTyping,
                displayedText = displayedText,
                onDisplayedTextUpdate = onDisplayedTextUpdate,
                onTypingStarted = onTypingStarted,
                index = index
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
    isSentOrRefreshed: Boolean,
    image: Bitmap?,
    onRemoveImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Show image above input if available
        if (image != null) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Start)
            ) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Field
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
                    .padding(end = 8.dp),
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

            // Image Select Button
            IconButton(
                onClick = onImageSelectClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_image_upload),
                    contentDescription = "Select Image",
                    tint = Color.White
                )
            }

            // Send or Stop button
            if (isSentOrRefreshed) {
                IconButton(
                    onClick = {
                        if (!isLoading) onStopClick()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .alpha(if (isStopButtonEnabled) 1f else 0.5f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icons8_stop_50),
                        contentDescription = "Stop",
                        tint = Color.White
                    )
                }
            } else {
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier.size(36.dp)
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
            .clickable { if (!isSentOrRefreshed || contentDescription[0] == 'C' || contentDescription[0] == 'S') onClick() } // Use the enabled state
            .padding(8.dp)
            .requiredSize(16.dp)
    )
}