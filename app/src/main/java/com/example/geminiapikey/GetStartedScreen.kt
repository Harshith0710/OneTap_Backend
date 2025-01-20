package com.example.geminiapikey

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat.getColor

@Composable
fun GetStartedScreen(context: Context) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(getColor(context, R.color.splash_background))) // Background color of the screen
    ) {
        IntroImage(R.drawable.group_1, 1.5f, Modifier.padding(top = (0.08f * screenHeight).dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Centering the content
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IntroImage(R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_1, 0.9f, Modifier.padding(top = (0.27f * screenHeight).dp))

            // Spacer between IntroText2 and Button
            Spacer(modifier = Modifier.weight(1f)) // Push the button to the bottom
            Text(
                text = "Welcome to QuickTap AI",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 24.sp
            )

            // Spacer between IntroText1 and IntroText2
            Spacer(modifier = Modifier.height(8.dp))

            // IntroText 2
            Text(
                text = "Your personal one touch AI Assistant",
                color = Color.Gray,
                fontSize = 16.sp
            )
            // GetStartedButton
            GetStartedButton(context = context)
        }
    }
}

@Composable
fun IntroImage(image: Int, sizeFraction: Float, modifier: Modifier) {
    BoxWithConstraints {
        val imageSize = (maxWidth * sizeFraction)
        Image(
            painter = painterResource(image),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = modifier.size((imageSize.value).dp)
        )
    }
}

@Composable
fun GetStartedButton(context: Context, modifier: Modifier = Modifier) {
    Button(
        onClick = {
            val intent = Intent(context, SwipeActivity::class.java)
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = modifier
            .navigationBarsPadding()
            .padding(top = 24.dp, bottom = 36.dp)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.content),
            contentDescription = null,
            modifier = Modifier.size(120.dp, 30.dp)
        )
    }
}
