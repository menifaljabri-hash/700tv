package com.tv700.player.ui.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tv700.player.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var logoVisible   by remember { mutableStateOf(false) }
    var textVisible   by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        logoVisible = true
        delay(600)
        textVisible = true
        delay(400)
        taglineVisible = true
        delay(1_800)
        onFinished()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ), label = "logoScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Navy700, Navy800, Navy900),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated glowing ring behind logo
        val glowAlpha by animateFloatAsState(
            targetValue = if (logoVisible) 0.25f else 0f,
            animationSpec = tween(1200), label = "glow"
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Gold500.copy(alpha = glowAlpha), Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo image
            AsyncImage(
                model = "file:///android_asset/logo_700tv.png",
                contentDescription = "700TV Logo",
                modifier = Modifier
                    .size(220.dp)
                    .scale(logoScale),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))

            // App name
            AnimatedVisibility(
                visible = textVisible,
                enter   = fadeIn(tween(500)) + slideInVertically { it / 2 }
            ) {
                Text(
                    text      = "700TV",
                    fontSize  = 52.sp,
                    fontWeight = FontWeight.Black,
                    color     = Gold500,
                    letterSpacing = 4.sp
                )
            }

            // Arabic tagline
            AnimatedVisibility(
                visible = taglineVisible,
                enter   = fadeIn(tween(600))
            ) {
                Text(
                    text      = "أفضل تجربة مشاهدة",
                    fontSize  = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color     = SharkWhite.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom version tag
        Text(
            text     = "v1.0",
            color    = SharkGray.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
