package com.example.geminiapikey

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TabItem(val title: String, val screen: @Composable () -> Unit)

@Composable
fun SwipeScreen(context : Context, onClick: () -> Unit) {
    val tabs = listOf(
        TabItem("Tab 1") { Tab1Screen() },
        TabItem("Tab 2") { Tab2Screen() },
        TabItem("Tab 3") { Tab3Screen(context, onClick) },
    )
    SwipableContent(tabs = tabs)
}

@Composable
fun SwipableContent(tabs: List<TabItem>) {
    val pagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0f) {
        tabs.size
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        tabs[page].screen()
    }
}

@Composable
fun Tab1Screen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Adjust to distribute content
    ) {
        // Top Content
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.tab11),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Image(
                painter = painterResource(R.drawable.tab_1_2),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.image_3),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Image(
                    painter = painterResource(R.drawable.image_2),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // Middle Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Seamless Assistance",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "QuickTap AI integrates effortlessly into your device, letting you harness AI power by simply long-pressing text - no need to switch apps!",
                color = Color.Gray,
                modifier = Modifier.padding(20.dp)
            )
        }

        // Bottom Row with Circles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp),
            horizontalArrangement = Arrangement.Center, // Center the circles horizontally
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(16.dp)) // Add space between circles
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(16.dp)) // Add space between circles
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }
    }
}

@Composable
fun Tab2Screen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Adjust to distribute content
    ) {
        // Top Content
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ellipse_2),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Image(
                painter = painterResource(R.drawable.ellipse_3),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Image(
                painter = painterResource(R.drawable.finger),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
        }

        // Middle Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Privacy First",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your data stays yours. QuickTap AI operates with a privacy-focused approach, ensuring secure and reliable assistance every time.",
                color = Color.Gray,
                modifier = Modifier.padding(20.dp)
            )
        }

        // Bottom Row with Circles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp),
            horizontalArrangement = Arrangement.Center, // Center the circles horizontally
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(16.dp)) // Add space between circles
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(16.dp)) // Add space between circles
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }
    }
}

@Composable
fun Tab3Screen(context: Context, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Content
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.tab11),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Image(
                painter = painterResource(R.drawable.tab_1_2),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.group_3),
                        contentDescription = null,
                        modifier = Modifier.size(90.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.group_2),
                        contentDescription = null,
                        modifier = Modifier.size(90.dp)
                    )
                }
                Image(
                    painter = painterResource(R.drawable.group_4),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp)
                )
            }
        }

        // Middle Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Smart Interactions",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Craft meaningful replies, gain instant insights, and stay productive. QuickTap AI is your personal assistant, always at your fingertips.",
                color = Color.Gray,
                modifier = Modifier.padding(20.dp)
            )
        }

        // Bottom Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                )
        ) {
            // Circles Row
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            // Enhanced Button
            var isPressed by remember { mutableStateOf(false) }
            val buttonColor by animateColorAsState(
                targetValue = Color.Black,
                label = ""
            )
            val buttonElevation by animateDpAsState(
                targetValue = if (isPressed) 12.dp else 8.dp,
                label = ""
            )
            Button(
                onClick = {
                    isPressed = !isPressed
                    onClick()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp)
                    .shadow(buttonElevation, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}