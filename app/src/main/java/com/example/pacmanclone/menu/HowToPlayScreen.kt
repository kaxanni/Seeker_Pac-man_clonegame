package com.example.pacmanclone.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HowToPlayScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("How to Play", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Game Modes:", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Text("- Multiplayer: Choose to play as Pacman or a Ghost and compete against other players.")
        Text("- Solo Mode: Play as Pacman against AI-controlled Ghosts.")
        Spacer(modifier = Modifier.height(12.dp))

        Text("Controls:", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Text("- Use the joystick or swipe to move Pacman.")
        Text("- Ghosts move automatically but can use their abilities strategically.")
        Spacer(modifier = Modifier.height(12.dp))

        Text("Solo Mode Mechanics:", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Text("- Collect 20 pellets to activate the speed boost power-up.")
        Text("- Collect 50 pellets to activate 5 seconds of immunity.")
        Spacer(modifier = Modifier.height(12.dp))

        Text("Multiplayer Mechanics:", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Text("- Players choose to play as Pacman or a Ghost.")
        Text("- Pacman has both immunity and speed boost power-ups.")
        Text("- Ghosts have a speed boost power-up with a 7-second cooldown.")
        Text("- Ghosts can invert Pacman's direction temporarily for 7 seconds.")
        Spacer(modifier = Modifier.height(12.dp))

        Text("Winning Conditions:", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Text("- As Pacman: Score the highest points or survive as long as possible.")
        Text("- As Ghost: Catch Pacman as quickly as possible.")
        Spacer(modifier = Modifier.height(12.dp))

        Text("Leaderboards:", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Text("- Multiplayer: Most wins as Pacman or Ghost.")
        Text("- Solo: Highest score as Pacman, fastest Pacman elimination as Ghost.")
    }
}
