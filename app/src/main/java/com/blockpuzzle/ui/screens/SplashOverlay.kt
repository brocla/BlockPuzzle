package com.blockpuzzle.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockpuzzle.R
import com.blockpuzzle.ui.theme.BoardDark
import kotlinx.coroutines.delay

@Composable
fun SplashOverlay(onFinished: () -> Unit) {
    // Fade in the content
    val contentAlpha = remember { Animatable(0f) }
    // Fade out the whole overlay
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Fade in icon + title
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        // Hold for a moment
        delay(1500)
        // Fade out entire overlay
        overlayAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overlayAlpha.value)
            .background(BoardDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(contentAlpha.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_block_puzzle),
                contentDescription = "Block Puzzle",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Julie's",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFFFFD54F),
                    letterSpacing = 3.sp,
                    shadow = Shadow(
                        color = Color(0x88000000),
                        offset = Offset(2f, 3f),
                        blurRadius = 4f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Block Puzzle",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFFFFD700),
                    letterSpacing = 2.sp,
                    shadow = Shadow(
                        color = Color(0x88000000),
                        offset = Offset(2f, 3f),
                        blurRadius = 4f
                    )
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
