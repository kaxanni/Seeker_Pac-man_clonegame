package com.example.pacmanclone.menu

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.pacmanclone.R
import kotlinx.coroutines.delay

@Composable
fun MainMenu(
    skipAnimation: Boolean,  // If true, skip the 3-second delay for the title
    onStartGame: () -> Unit
) {
    val context = LocalContext.current

    // Main menu background image and retro font
    val backgroundPainter = painterResource(id = R.drawable.main_background)
    val retroFontFamily = FontFamily(Font(R.font.retro_font))

    // Title offset states
    var titleOffset by remember { mutableStateOf(0.dp) }
    var showMenuBox by remember { mutableStateOf(false) }
    var startTransition by remember { mutableStateOf(false) }

    // Animate the title offset
    val animatedTitleOffset by animateDpAsState(
        targetValue = titleOffset,
        animationSpec = tween(durationMillis = 1000), label = ""
    )
    // Fade out entire menu on game start
    val transitionAlpha by animateFloatAsState(
        targetValue = if (startTransition) 0f else 1f,
        animationSpec = tween(durationMillis = 1000), label = ""
    )
    // Slide entire menu up on game start
    val transitionOffset by animateDpAsState(
        targetValue = if (startTransition) (-200).dp else 0.dp,
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    // Decide if we run the 3-second delay animation
    LaunchedEffect(skipAnimation) {
        if (!skipAnimation) {
            delay(3000) // Normal 3-second wait
        }
        // Slide title up and show box
        titleOffset = 1.dp
        showMenuBox = true
    }

    // Transition to the game after fade/slide
    LaunchedEffect(startTransition) {
        if (startTransition) {
            delay(1000)
            onStartGame()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Image(
                painter = backgroundPainter,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Semi-transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f))
            )
            // Main menu content with fade/slide transition
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .offset(y = transitionOffset)
                    .alpha(transitionAlpha),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title "SEEKER"
                Text(
                    text = "SEEKER",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = retroFontFamily,
                        color = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.offset(y = animatedTitleOffset)
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Dungeon-tile–themed box with buttons
                AnimatedVisibility(
                    visible = showMenuBox,
                    enter = fadeIn(animationSpec = tween(durationMillis = 1000))
                ) {
                    Box(
                        modifier = Modifier
                            .width(320.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dungeon tile background
                        Image(
                            painter = painterResource(id = R.drawable.dungeon_tile),
                            contentDescription = "Dungeon Tile Background",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        // Optional dim overlay
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f))
                        )
                        // Buttons column
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .width(200.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { startTransition = true }
                            ) {
                                Text(text = "Solo Player")
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { /* Multiplayer */ }
                            ) {
                                Text(text = "Multiplayer")
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { /* Leaderboard */ }
                            ) {
                                Text(text = "Leaderboard")
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { /* Options */ }
                            ) {
                                Text(text = "Options")
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    (context as? Activity)?.finishAffinity()
                                }
                            ) {
                                Text(text = "Exit")
                            }
                        }
                    }
                }
            }
        }
    }
}
