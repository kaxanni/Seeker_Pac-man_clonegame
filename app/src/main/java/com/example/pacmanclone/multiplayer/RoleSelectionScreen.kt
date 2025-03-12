package com.example.pacmanclone.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoleSelectionScreen(
    onRoleChosen: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose Your Role")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRoleChosen("pacman") }) {
            Text("Play as Pac-Man")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRoleChosen("ghost") }) {
            Text("Play as Ghost")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
