package com.example.pacmanclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.pacmanclone.auth.AccountScreen
import com.example.pacmanclone.game.GameLaunchAnimation
import com.example.pacmanclone.game.PacmanGame
import com.example.pacmanclone.menu.HowToPlayScreen
import com.example.pacmanclone.menu.LeaderboardScreen
import com.example.pacmanclone.menu.MainMenu
import com.example.pacmanclone.menu.OptionsScreen
import com.example.pacmanclone.menu.SoundSettingsScreen
import com.example.pacmanclone.multiplayer.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MyApp() }
    }
}

@Composable
fun MyApp() {
    var showMainMenu by remember { mutableStateOf(true) }
    var showOptions by remember { mutableStateOf(false) }
    var showGame by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var showLaunch by remember { mutableStateOf(false)  }
    var showLeaderboard by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var showSoundSettings by remember { mutableStateOf(false) }

    // Multiplayer states
    var showRoleSelection by remember { mutableStateOf(false) }
    var showMatchmaking by remember { mutableStateOf(false) }
    var showMultiplayerGame by remember { mutableStateOf(false) }
    var chosenRole by remember { mutableStateOf<String?>(null) }
    var multiplayerMatch by remember { mutableStateOf<MultiplayerMatch?>(null) }

    when {
        showGame -> {
            PacmanGame(onBack = {
                showGame = false
                showMainMenu = true
            })
        }
        showLaunch -> {
            GameLaunchAnimation ( onAnimationComplete = {
                showLaunch = false
                showGame = true
            } )
        }
        showOptions -> {
            OptionsScreen(
                onAccountsClicked = {
                    showOptions =false
                    showAccount = true
                },
                onHowToPlayClicked = {
                    // Move from Options to the how-to-play screen
                    showOptions = false
                    showHowToPlay = true
                },
                onSoundsClicked = {
                    showOptions = false
                    showSoundSettings = true
                },
                onBack = {
                    showOptions = false
                    showMainMenu = true
                }
            )
        }
        showHowToPlay -> {
            // Show your HowToPlayScreen
            HowToPlayScreen(
                onBack = {
                    // Return to OptionsScreen or main menu, your choice
                    showHowToPlay = false
                    showOptions = true
                },
                retroFontFamily = retroFontFamily
            )
        }
        // Sound settings screen.
        showSoundSettings -> {
            SoundSettingsScreen(
                onBack = {
                    showSoundSettings = false
                    showOptions = true
                },
                retroFontFamily = retroFontFamily
            )
        }
        showAccount -> {
            AccountScreen(
                onBack = {
                    showAccount = false
                    showOptions = true
                },
                onLoggedIn = {
                    showAccount = false
                    showMainMenu = true
                }
            )
        }
        showLeaderboard -> {
            LeaderboardScreen(onBack = {
                showLeaderboard = false
                showMainMenu = true
            })
        }
        showRoleSelection -> {
            RoleSelectionScreen(
                onRoleChosen = { role ->
                    chosenRole = role
                    showRoleSelection = false
                    showMatchmaking = true
                },
                onBack = {
                    showRoleSelection = false
                    showMainMenu = true
                }
            )
        }
        showMatchmaking && chosenRole != null -> {
            MatchmakingScreen(
                selectedRole = chosenRole!!,
                onMatchFound = { match ->
                    if (match != null) {
                        multiplayerMatch = match
                        showMatchmaking = false
                        showMultiplayerGame = true
                    } else {
                        showMatchmaking = false
                        showMainMenu = true
                    }
                },
                onBack = {
                    showMatchmaking = false
                    showMainMenu = true
                }
            )
        }
        showMultiplayerGame && multiplayerMatch != null -> {
            MultiplayerGame(
                match = multiplayerMatch!!,
                onBack = {
                    showMultiplayerGame = false
                    showMainMenu = true
                }
            )
        }
        showMainMenu -> {
            MainMenu(
                skipAnimation = false,
                onStartGame = {
                    showMainMenu = false
                    showGame = true
                },
                onOptionClicked = {
                    showMainMenu = false
                    showOptions = true
                },
                onLeaderboardClicked = {
                    showMainMenu = false
                    showLeaderboard = true
                },
                onMultiplayerClicked = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        // handle login if necessary
                    } else {
                        showMainMenu = false
                        showRoleSelection = true
                    }
                }
            )
        }
    }
}
