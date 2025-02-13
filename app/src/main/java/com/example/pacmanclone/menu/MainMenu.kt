// MainMenu.kt
package com.example.pacmanclone.menu

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MainMenu(onStartGame: () -> Unit) {
    // Get the current Activity context to allow exiting the app.
    val context = LocalContext.current

    // Use a full-screen Surface to center the content.
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title at the top.
            Text(
                text = "Pac‑Man",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            // A centered Box that acts as the menu container.
            Box(
                modifier = Modifier
                    .width(300.dp) // Fixed container width.
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Column inside the box to arrange the buttons.
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        modifier = Modifier.width(250.dp), // Fixed button width.
                        onClick = onStartGame
                    ) {
                        Text(text = "Solo Player")
                    }
                    Button(
                        modifier = Modifier.width(250.dp),
                        onClick = { /* TODO: Implement Multiplayer functionality */ }
                    ) {
                        Text(text = "Multiplayer")
                    }
                    Button(
                        modifier = Modifier.width(250.dp),
                        onClick = { /* TODO: Implement Leaderboard functionality */ }
                    ) {
                        Text(text = "Leaderboard")
                    }
                    Button(
                        modifier = Modifier.width(250.dp),
                        onClick = { /* TODO: Implement Options functionality */ }
                    ) {
                        Text(text = "Options")
                    }
                    Button(
                        modifier = Modifier.width(250.dp),
                        onClick = {
                            // Exit the app by finishing the current Activity.
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
