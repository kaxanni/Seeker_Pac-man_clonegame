package com.example.pacmanclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.pacmanclone.auth.AccountScreen
import com.example.pacmanclone.game.GameLaunchAnimation
import com.example.pacmanclone.game.PacmanGame
import com.example.pacmanclone.menu.MainMenu
import com.example.pacmanclone.menu.OptionsScreen
import com.example.pacmanclone.menu.HowToPlayScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    // State booleans controlling which screen is shown
    var showMainMenu by remember { mutableStateOf(true) }
    var showOptions by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }

    var showLaunch by remember { mutableStateOf(false) } // The second screen (Knights door, etc.)
    var showGame by remember { mutableStateOf(false) }   // The actual PacmanGame

    when {
        // 1) Show the actual Pac-Man game if showGame is true
        showGame -> {
            PacmanGame(
                onBack = {
                    // If user presses "Back" in PacmanGame, go to main menu
                    showGame = false
                    showMainMenu = true
                }
            )
        }
        // 2) Show the second screen (GameLaunchAnimation) if showLaunch is true
        showLaunch -> {
            GameLaunchAnimation(
                onAnimationComplete = {
                    // Once animation is done, proceed to the PacmanGame
                    showLaunch = false
                    showGame = true
                }
            )
        }
        // 3) Show the AccountScreen if showAccount is true
        showAccount -> {
            AccountScreen(
                onBack = {
                    // Return to Options after back
                    showAccount = false
                    showOptions = true
                },
                onLoggedIn = {
                    // Once logged in, go back to main menu
                    showAccount = false
                    showMainMenu = true
                }
            )
        }
        // 4) Show the HowToPlayScreen if showHowToPlay is true
        showHowToPlay -> {
            HowToPlayScreen(onBack = {
                showHowToPlay = false
                showOptions = true // Or navigate back to the appropriate screen
            })
        }

        // 5) Show the OptionsScreen if showOptions is true
        showOptions -> {
            // Make sure your OptionsScreen has these four parameters
            OptionsScreen(
                onAccountsClicked = {
                    // Navigate to AccountScreen
                    showOptions = false
                    showAccount = true
                },
                onSoundsClicked = {
                    // TODO: handle sound settings
                },
                onHowToPlayClicked = {
                    // Navigate to HowToPlayScreen
                    showOptions = false
                    showHowToPlay = true
                },
                onBack = {
                    // Return to main menu
                    showOptions = false
                    showMainMenu = true
                }
            )
        }
        // 6) Otherwise, show the main menu
        showMainMenu -> {
            MainMenu(
                skipAnimation = false,
                onStartGame = {
                    // Tapping "Solo Player" triggers this:
                    // 1) Hide main menu
                    // 2) Show GameLaunchAnimation
                    showMainMenu = false
                    showLaunch = true
                },
                onOptionClicked = {
                    // Show the Options screen
                    showMainMenu = false
                    showOptions = true
                }
            )
        }
    }
}
