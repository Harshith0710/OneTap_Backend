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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.sharp.Send
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val promptResponseList = remember { mutableStateListOf<Triple<String, String, Bitmap?>>() }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    context as? Activity

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val bitmap = getBitmapFromUri(context.contentResolver, it)
                selectedImage = bitmap
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(getColor(context, R.color.splash_background)))
            .windowInsetsPadding(WindowInsets.safeContent.union(WindowInsets.navigationBars))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
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
                    promptResponseList = promptResponseList
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
                    if(prompt!=""){
                        bakingViewModel.sendPrompt(selectedImage, prompt)
                        promptResponseList.add(Triple(prompt, "", selectedImage))
                        prompt = ""
                        selectedImage = null
                    }
                },
                onImageSelectClick = {
                    imageLauncher.launch("image/*")
                }
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
fun ContentSection(
    context: Context,
    uiState: UiState,
    promptResponseList: MutableList<Triple<String, String, Bitmap?>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        promptResponseList.forEachIndexed { _, triple ->
            val (currentPrompt, currentResponse, currentImage) = triple

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
                },
                uploadedImage = currentImage
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

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
    onShare: () -> Unit,
    uploadedImage: Bitmap?
) {
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
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = prompt,
                color = Color.White,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

        }
        uploadedImage?.let {
            Image(
                bitmap = it.asImageBitmap(),
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
            Image(
                painter = painterResource(id = R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_2),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(36.dp)
            )

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
            }
        }

        TypingEffectText(
            fullText = response,
            typingSpeed = 30L
        )

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
    onImageSelectClick: () -> Unit
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
    typingSpeed: Long = 30L
) {
    var animatedText by remember { mutableStateOf("") }

    LaunchedEffect(fullText) {
        animatedText = ""
        for (char in fullText) {
            animatedText += char
            delay(typingSpeed)
        }
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
        style = TextStyle(fontSize = 16.sp),
        modifier = Modifier.padding(16.dp)
    )
}



@Composable
fun Header(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
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
        Spacer(modifier = Modifier.weight(0.8f))
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