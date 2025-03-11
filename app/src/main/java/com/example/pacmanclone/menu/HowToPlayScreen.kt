package com.example.pacmanclone.menu


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacmanclone.R

@Composable
fun HowToPlayScreen(onBack: () -> Unit, retroFontFamily: FontFamily) {
    val scrollState = rememberScrollState()
    val background: Painter = painterResource(id = R.drawable.howtoplay)
    val backButton: Painter = painterResource(id = R.drawable.back_button)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = background,
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = backButton,
                    contentDescription = "Back Button",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onBack() }
                )
            }

            Text("How to Play", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = retroFontFamily)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Game Modes:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("- Multiplayer: Choose to play as Pacman or a Ghost and compete against other players.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- Solo Mode: Play as Pacman against AI-controlled Ghosts.", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Controls:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("- Use the joystick or swipe to move Pacman.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- Ghosts move automatically but can use their abilities strategically.", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Solo Mode Mechanics:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("- Collect 20 pellets to activate the speed boost power-up.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- Collect 50 pellets to activate 5 seconds of immunity.", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Multiplayer Mechanics:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("- Players choose to play as Pacman or a Ghost.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- Pacman has both immunity and speed boost power-ups.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- Ghosts have a speed boost power-up with a 7-second cooldown.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- Ghosts can invert Pacman's direction temporarily for 7 seconds.", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Winning Conditions:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("- As Pacman: Score the highest points or survive as long as possible.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- As Ghost: Catch Pacman as quickly as possible.", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Leaderboards:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("- Multiplayer: Most wins as Pacman or Ghost.", color = Color.Black, fontWeight = FontWeight.Bold)
            Text("- Solo: Highest score as Pacman, fastest Pacman elimination as Ghost.", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}