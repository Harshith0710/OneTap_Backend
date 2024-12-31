package com.example.geminiapikey

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onActionClick: (String) -> Unit) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            QuickTapUIScreen(onActionClick)
        }
    }
}

@Composable
fun QuickTapUIScreen(onActionClick: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val buttonWidth = screenWidth / 2
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF101010), Color(0xFF282828))
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.frame_3),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp)

            )
            Image(
                painter = painterResource(id = R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_2),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ellipse_78),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hi, Mukesh 👋",
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "How may I help you today?",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonWidth + 4.dp)
            ) {
                ActionButton("Chat with Bot", R.drawable.frame_5, Color(0xFFB39DDB), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth / 2))
                Spacer(Modifier.height(4.dp))
                ActionButton("Search by Image", R.drawable.frame_5__1_, Color(0xFFA5D6A7), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth / 2))
            }
            Spacer(Modifier.padding(2.dp))
            Column(
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonWidth + 4.dp)
                    .padding(2.dp)
            ) {
                ActionButton("Talk with Bot", R.drawable.frame_5__2_, Color(0xFFF48FB1), onActionClick,
                    Modifier
                        .fillMaxWidth()
                        .height(buttonWidth + 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Fun Things To Do",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FunOption("Lyrics", "Generate lyrics of a song for any music genre.")
            FunOption("Reply Writer", "Write an awesome reply to messages, emails and more.")
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    image: Int,
    color: Color,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, shape = RoundedCornerShape(16.dp))
            .clickable { onActionClick(text) }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(image),
                    contentDescription = "Action Icon",
                    modifier = Modifier.size(32.dp)
                )
                Image(
                    painter = painterResource(R.drawable._icon__arrow_forward_),
                    contentDescription = "Forward Arrow",
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun FunOption(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = description,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}
