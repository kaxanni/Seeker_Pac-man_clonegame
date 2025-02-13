package com.example.pacmanclone


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pacmanclone.game.PacmanGame
import com.example.pacmanclone.ui.theme.PacmanCloneTheme
import com.example.pacmanclone.menu.MainMenu

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PacmanCloneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showMenu by remember { mutableStateOf(true) }
                    if (showMenu) {
                        MainMenu(onStartGame = { showMenu = false })
                    } else {
                        // Pass the lambda that sets showMenu to true.
                        PacmanGame(onBack = { showMenu = true })
                    }
                }
            }
        }
    }
}
