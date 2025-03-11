package com.example.pacmanclone.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.pacmanclone.R
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

    // Whether we've loaded the user's document from Firestore
    var userDocLoaded by remember { mutableStateOf(false) }
    // The stored username (empty if none)
    var username by remember { mutableStateOf("") }

    // Load username from Firestore once
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    userDocLoaded = true
                    if (snapshot.exists()) {
                        val fetched = snapshot.getString("username") ?: ""
                        username = fetched
                    }
                }
                .addOnFailureListener {
                    userDocLoaded = true
                    username = ""
                }
        } else {
            userDocLoaded = true
            username = ""
        }
    }

    // If not loaded, show nothing (or a loading spinner if you want)
    if (!userDocLoaded) return

    if (username.isEmpty()) {
        // The user has no username => dark overlay + center input
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)) // darken the screen
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            var newUsername by remember { mutableStateOf(TextFieldValue("")) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // The nametag box + BasicTextField
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // The background image
                    Image(
                        painter = painterResource(id = R.drawable.nametag_box),
                        contentDescription = "Nametag Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // A BasicTextField with a decorationBox to center placeholder & text
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
                            .padding(horizontal = 12.dp), // some padding inside the box
                        decorationBox = { innerTextField ->
                            // Another box to center the placeholder / typed text
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // If empty, show placeholder
                                if (newUsername.text.isEmpty()) {
                                    Text(
                                        text = "Enter Username",
                                        fontFamily = retroFontFamily,
                                        color = Color.LightGray,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                // Draw the actual typed text
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                // "Save" button
                Button(onClick = {
                    if (currentUser != null && newUsername.text.isNotBlank()) {
                        db.collection("users").document(currentUser.uid)
                            .set(mapOf("username" to newUsername.text))
                            .addOnSuccessListener {
                                // Once saved, set local username => hide input
                                username = newUsername.text
                            }
                    }
                }) {
                    Text("Save", fontFamily = retroFontFamily)
                }
            }
        }
    } else {
        // The user already has a username => show "Hello, <username>" in top-left
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "Hello, $username",
                fontFamily = retroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
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
    // Full screen background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.1f))
    ) {
        // Background image for the options screen
        Image(
            painter = painterResource(id = R.drawable.options_background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
        )
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
