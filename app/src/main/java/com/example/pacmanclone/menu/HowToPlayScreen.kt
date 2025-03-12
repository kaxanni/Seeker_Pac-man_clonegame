package com.example.pacmanclone.menu

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.pacmanclone.R
import com.example.pacmanclone.util.MusicController

@Composable
fun HowToPlayScreen(onBack: () -> Unit, retroFontFamily: FontFamily) {
    val scrollState = rememberScrollState()
    val background: Painter = painterResource(id = R.drawable.howtoplay)
    val backButton: Painter = painterResource(id = R.drawable.back_button)
    val context = LocalContext.current

    // Create and start background music
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.option_music) // your .mp3 in res/raw
    }
    DisposableEffect(Unit) {
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
    // Observe global volume/mute from MusicController
    LaunchedEffect(MusicController.volume, MusicController.isMuted) {
        if (MusicController.isMuted) {
            mediaPlayer.setVolume(0f, 0f)
        } else {
            mediaPlayer.setVolume(MusicController.volume, MusicController.volume)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1) Full-screen background image
        Image(
            painter = background,
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // 2) Semi-transparent overlay covering the entire screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)) // Adjust alpha as needed
        )

        // 3) Main content (column with text), scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button in a row or box
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = backButton,
                    contentDescription = "Back Button",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onBack() }
                )
            }

            // Title
            Text(
                "How to Play",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,  // keep black text
                fontFamily = retroFontFamily
            )
            Spacer(modifier = Modifier.height(16.dp))

            // The rest of your instructions
            Text("Game Modes:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("- Multiplayer: Choose to play as Pacman or a Ghost...", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- Solo Mode: Play as Pacman against AI-controlled Ghosts.", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Controls:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("- Use the joystick or swipe to move Pacman.", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- Ghosts move automatically...", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Solo Mode Mechanics:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("- Collect 20 pellets to activate the speed boost power-up.", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- Collect 50 pellets to activate 5 seconds of immunity.", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Multiplayer Mechanics:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("- Players choose to play as Pacman or a Ghost...", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- Pacman has both immunity and speed boost power-ups.", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- Ghosts have a speed boost power-up with a 7-second cooldown.", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- Ghosts can invert Pacman's direction temporarily for 7 seconds.", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Winning Conditions:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("- As Pacman: Score the highest points...", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- As Ghost: Catch Pacman as quickly as possible.", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Leaderboards:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("- Multiplayer: Most wins as Pacman or Ghost.", color = Color.White, fontWeight = FontWeight.Bold)
            Text("- Solo: Highest score as Pacman, fastest Pacman elimination as Ghost.", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
