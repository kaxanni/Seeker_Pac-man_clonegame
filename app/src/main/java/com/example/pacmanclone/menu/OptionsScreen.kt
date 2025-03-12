package com.example.pacmanclone.menu

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.example.pacmanclone.R
import com.example.pacmanclone.util.MusicController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun OptionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(80.dp)
            .width(400.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.option_button),
            contentDescription = "Button Background",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = retroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * This composable handles loading & saving the username.
 *  - If username is not set, shows a centered input field on top (zIndex).
 *  - Once saved, it shows "Hi, username" at the top-left edge of the screen.
 */
@Composable
fun UsernameOverlay() {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    if (currentUser == null) return

    var userDocLoaded by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(currentUser.uid) {
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { snapshot ->
                userDocLoaded = true
                if (snapshot.exists()) {
                    val fetched = snapshot.getString("username") ?: ""
                    username = fetched
                    newUsername = TextFieldValue(fetched)
                }
            }
            .addOnFailureListener {
                userDocLoaded = true
                username = ""
            }
    }

    if (!userDocLoaded) return

    if (username.isEmpty() || isEditing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nametag_box),
                        contentDescription = "Nametag Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    BasicTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = retroFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (newUsername.text.isEmpty()) {
                                    Text(
                                        text = "Enter Username",
                                        fontFamily = retroFontFamily,
                                        color = Color.LightGray,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    if (newUsername.text.isNotBlank()) {
                        db.collection("users").document(currentUser.uid)
                            .set(mapOf("username" to newUsername.text))
                            .addOnSuccessListener {
                                username = newUsername.text
                                isEditing = false
                            }
                    }
                }) {
                    Text("Save", fontFamily = retroFontFamily)
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "Hi, $username",
                style = TextStyle(
                    fontFamily = retroFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        newUsername = TextFieldValue(username)
                        isEditing = true
                    }
            )
        }
    }
}

/**
 * The main OptionsScreen layout:
 *  - Stone box with 4 buttons
 *  - A "UsernameOverlay" composable on top to handle username logic
 */
@Composable
fun OptionsScreen(
    onAccountsClicked: () -> Unit,
    onSoundsClicked: () -> Unit,
    onHowToPlayClicked: () -> Unit,
    onBack: () -> Unit
) {
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



    // **Add a background image** behind everything:
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.option_background), // CHANGE to your actual image
            contentDescription = "Options Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Then your existing layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            // Stone-themed box
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(450.dp)
                    .align(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.option_box),
                    contentDescription = "Option Box",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                // The 4 option buttons
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OptionButton(text = "Accounts", onClick = onAccountsClicked)
                    OptionButton(text = "Sounds", onClick = onSoundsClicked)
                    OptionButton(text = "How to Play", onClick = onHowToPlayClicked)
                    OptionButton(text = "Back", onClick = onBack)
                }
            }

            // The username overlay always on top (zIndex) so it won't hide behind anything
            UsernameOverlay()
        }
    }
}
