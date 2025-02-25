package com.example.pacmanclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pacmanclone.menu.MainMenu
import com.example.pacmanclone.game.GameLaunchAnimation
import com.example.pacmanclone.game.PacmanGame

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Navigation states:
                    // showMenu -> main menu
                    // showLaunch -> optional game launch animation
                    // showGame -> actual game screen
                    var showMenu by remember { mutableStateOf(true) }
                    var showLaunch by remember { mutableStateOf(false) }
                    var showGame by remember { mutableStateOf(false) }

                    // Flag to skip main menu animations after returning from the game
                    var skipMenuAnimation by remember { mutableStateOf(false) }

                    when {
                        showMenu -> {
                            MainMenu(
                                skipAnimation = skipMenuAnimation,
                                onStartGame = {
                                    // Hide menu, show optional launch animation
                                    showMenu = false
                                    showLaunch = true
                                    // Next time user returns to menu, skip animations
                                    skipMenuAnimation = true
                                }
                            )
                        }
                        showLaunch -> {
                            // Optional short animation before game
                            GameLaunchAnimation(onAnimationComplete = {
                                showLaunch = false
                                showGame = true
                            })
                        }
                        showGame -> {
                            PacmanGame(onBack = {
                                // Returning to the main menu from the game
                                showGame = false
                                showMenu = true
                                // We remain with skipMenuAnimation = true
                            })
                        }
                    }
                }
            }
        }
    }
}
