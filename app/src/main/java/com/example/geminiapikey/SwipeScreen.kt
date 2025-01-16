package com.example.geminiapikey

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TabItem(val title: String, val screen: @Composable () -> Unit)

@Composable
fun SwipeScreen(context : Context) {
    val tabs = listOf(
        TabItem("Tab 1") { Tab1Screen() },
        TabItem("Tab 2") { Tab2Screen() },
        TabItem("Tab 3") { Tab3Screen(context) },
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
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.tab11),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(),
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
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Seamless Assistance",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text= "QuickTap AI integrates effortlessly into your device, letting you harness AI power by simply long-pressing text - no need to switch apps!",
            color = Color.Gray,
            modifier = Modifier.padding(20.dp)
        )
        Spacer(modifier = Modifier.height(400.dp))
    }
}

@Composable
fun Tab2Screen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.tab11),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Image(
                painter = painterResource(R.drawable.tab_1_2),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )
            Box(
                contentAlignment = Alignment.Center
            ){
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
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Smart Interactions",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text= "Craft meaningful replies, gain instant insights, and stay productive. QuickTap AI is your personal assistant, always at your fingertips.",
            color = Color.Gray,
            modifier = Modifier.padding(20.dp)
        )
        Spacer(modifier = Modifier.height(400.dp))
    }
}

@Composable
fun Tab3Screen(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ellipse_2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(),
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
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Privacy First",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text= "Your data stays yours. QuickTap AI operates with a privacy-focused approach, ensuring secure and reliable assistance every time.",
            color = Color.Gray,
            modifier = Modifier.padding(20.dp)
        )
        Button(
            onClick = {
                context.startActivity(Intent(context,HomeActivity::class.java))
            }
        ) {
            Text("Next")
        }
        Spacer(modifier = Modifier.height(300.dp))
    }
}
