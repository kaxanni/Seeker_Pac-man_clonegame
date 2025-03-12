package com.example.pacmanclone.menu

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacmanclone.R
import com.example.pacmanclone.util.MusicController

@Composable
fun SoundSettingsScreen(onBack: () -> Unit, retroFontFamily: FontFamily) {
    // Read current volume and mute state from the global MusicController.
    var volume by remember { mutableStateOf(MusicController.volume) }
    var isMuted by remember { mutableStateOf(MusicController.isMuted) }

    val context = LocalContext.current

    // Create the MediaPlayer and start music
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.option_music) // replace with your mp3 name
    }
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

    // Update the global values when these change.
    LaunchedEffect(volume, isMuted) {
        MusicController.volume = volume
        MusicController.isMuted = isMuted
    }

    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        // Use main menu background image.
        Image(
            painter = painterResource(id = R.drawable.main_background),
            contentDescription = "Sound Settings Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Semi-transparent overlay.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        // Main content.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sound Settings",
                fontSize = 28.sp,
                color = Color.White,
                fontFamily = retroFontFamily
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Volume:",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontFamily = retroFontFamily
                )
                Spacer(modifier = Modifier.width(16.dp))
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.width(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isMuted,
                    onCheckedChange = { isMuted = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mute",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontFamily = retroFontFamily
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onBack) {
                Text(
                    text = "Back",
                    fontSize = 18.sp,
                    fontFamily = retroFontFamily
                )
            }
        }
    }
}
