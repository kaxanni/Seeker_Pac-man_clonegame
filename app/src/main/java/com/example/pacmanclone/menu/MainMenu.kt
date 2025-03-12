package com.example.pacmanclone.menu

import android.app.Activity
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacmanclone.R
import com.example.pacmanclone.util.MusicController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

// Define a custom font family for a retro-styled text
val retroFontFamily = FontFamily(Font(R.font.retro_font))

@Composable
fun MainMenuUsernameDisplay() {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    loaded = true
                    if (snapshot.exists()) {
                        username = snapshot.getString("username") ?: ""
                    }
                }
                .addOnFailureListener {
                    loaded = true
                }
        } else {
            loaded = true
        }
    }

    if (loaded && username.isNotEmpty()) {
        Text(
            text = "Hello, $username",
            fontFamily = retroFontFamily,
            fontSize = 16.sp,
            color = androidx.compose.ui.graphics.Color.White
        )
    }
}

// Custom composable for a wooden-styled button
@Composable
fun WoodenButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(50.dp)
            .width(180.dp)
            .clickable(onClick = onClick), // Button click action
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.stone_button), // Background image for button
            contentDescription = "Button Background",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = text,
            color = androidx.compose.ui.graphics.Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = retroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp, // Adjusted text size
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * MainMenu now has an extra parameter [onOptionClicked]
 * so the "Option" button can do something (e.g., show an Options screen).
 */
@Composable
fun MainMenu(
    skipAnimation: Boolean, // Boolean to skip intro animation
    onStartGame: () -> Unit, // Callback when the game starts
    onOptionClicked: () -> Unit, // NEW: callback for "Option" button
    onLeaderboardClicked: () -> Unit,  // NEW callnack on leaderboard
    onMultiplayerClicked: () -> Unit // callback on multiplayer
) {
    val context = LocalContext.current // Retrieve the current context
    val backgroundPainter = painterResource(id = R.drawable.main_background) // Background image

    // Start the background music when MainMenu appears.
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.mainback_music) }
    DisposableEffect(Unit) {
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
    LaunchedEffect(MusicController.volume, MusicController.isMuted) {
        if (MusicController.isMuted) {
            mediaPlayer.setVolume(0f, 0f)
        } else {
            mediaPlayer.setVolume(MusicController.volume, MusicController.volume)
        }
    }



    var titleOffset by remember { mutableStateOf(0.dp) } // Animation offset for title
    var showMenuBox by remember { mutableStateOf(false) } // Controls menu box visibility
    var startTransition by remember { mutableStateOf(false) } // Controls transition animation

    // Animate title offset for smooth appearance
    val animatedTitleOffset by animateDpAsState(
        targetValue = titleOffset,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )
    // Animate fade out transition for the main menu
    val transitionAlpha by animateFloatAsState(
        targetValue = if (startTransition) 0f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )
    // Animate offset for main menu during transition
    val transitionOffset by animateDpAsState(
        targetValue = if (startTransition) (-200).dp else 0.dp,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )

    // Delayed menu reveal effect
    LaunchedEffect(skipAnimation) {
        if (!skipAnimation) {
            delay(3000) // Wait before revealing menu
        }
        titleOffset = 1.dp // Move title slightly
        showMenuBox = true // Show menu options
    }

    // Delayed transition when starting game
    LaunchedEffect(startTransition) {
        if (startTransition) {
            delay(1000) // Allow transition animation
            onStartGame()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            Image(
                painter = backgroundPainter,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Dark overlay for better contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .offset(y = transitionOffset)
                    .alpha(transitionAlpha),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show the user’s name if available
                MainMenuUsernameDisplay()

                // Game title
                Text(
                    text = "SEEKER",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = retroFontFamily,
                        color = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.offset(y = animatedTitleOffset)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Animated visibility for menu box
                AnimatedVisibility(
                    visible = showMenuBox,
                    enter = fadeIn(animationSpec = tween(durationMillis = 1000))
                ) {
                    Box(
                        modifier = Modifier
                            .width(500.dp)
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Wooden-themed menu background
                        Image(
                            painter = painterResource(id = R.drawable.stone_background),
                            contentDescription = "Wooden Background",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(50.dp))

                            // First row: Solo & Multiplayer buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                WoodenButton(text = "Solo Player", onClick = { startTransition = true })
                                Spacer(modifier = Modifier.width(16.dp))
                                WoodenButton(text = "Multiplayer", onClick = { onMultiplayerClicked() })
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Second row: Leaderboard & Options buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                WoodenButton(text = "Leaderboard", onClick = { onLeaderboardClicked() })
                                Spacer(modifier = Modifier.width(16.dp))
                                WoodenButton(text = "Option", onClick = { onOptionClicked() })
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Exit button
                            WoodenButton(
                                text = "Exit",
                                onClick = { (context as? Activity)?.finishAffinity() }, // Close app
                                modifier = Modifier.fillMaxWidth(0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
