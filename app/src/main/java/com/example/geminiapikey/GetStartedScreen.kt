package com.example.geminiapikey

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat.getColor

@Composable
fun GetStartedScreen(context: Context) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(getColor(context,R.color.splash_background)))
    ) {
        val (introImage, introText1, introText2, getStartedButton) = createRefs()
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .constrainAs(introImage) {
                    top.linkTo(parent.top, margin = 80.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.percent(0.9f)
                }
        ) {
            IntroImage(R.drawable.group_1, 1.5f)
            IntroImage(R.drawable.screenshot_from_2024_11_26_17_34_39_transformed_transformed_1, 0.9f)
        }

        IntroText(
            image = R.drawable.frame_38,
            sizeFraction = 0.8f,
            modifier = Modifier.constrainAs(introText1) {
                top.linkTo(introImage.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        IntroText(
            image = R.drawable.your_personal_one_touch_ai_assistant,
            sizeFraction = 0.6f,
            modifier = Modifier.constrainAs(introText2) {
                top.linkTo(introText1.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        GetStartedButton(
            context = context,
            modifier = Modifier.constrainAs(getStartedButton) {
                bottom.linkTo(parent.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.percent(0.6f)
            }
        )
    }
}

@Composable
fun IntroImage(image: Int, sizeFraction: Float) {
    BoxWithConstraints {
        val imageSize = (maxWidth * sizeFraction) // Convert to Dp
        Image(
            painter = painterResource(image),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size((imageSize.value).dp)
        )
    }
}

@Composable
fun IntroText(image: Int, sizeFraction: Float, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val textWidth = (maxWidth * sizeFraction) // Convert to Dp
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.size((textWidth.value).dp, 30.dp)
        )
    }
}

@Composable
fun GetStartedButton(context: Context, modifier: Modifier = Modifier) {
    Button(
        onClick = {
            val intent = Intent(context,BakingActivity::class.java)
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = modifier
            .padding(16.dp)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.content),
            contentDescription = null,
            modifier = Modifier.size(120.dp,30.dp)
        )
    }
}