@file:Suppress("DEPRECATION")

package com.example.pacmanclone.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacmanclone.R
import com.example.pacmanclone.menu.retroFontFamily
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSignUp by remember { mutableStateOf(false) }

    val webClientId = stringResource(R.string.default_web_client_id)
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(gso) {
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account != null) {
                    val idToken = account.idToken
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authResult ->
                            if (authResult.isSuccessful) {
                                onLoggedIn()
                            } else {
                                errorMsg = authResult.exception?.localizedMessage ?: "Google Sign-In failed"
                            }
                        }
                } else {
                    errorMsg = "GoogleSignInAccount is null"
                }
            } catch (e: ApiException) {
                errorMsg = e.localizedMessage ?: "Google Sign-In error"
            }
        } else {
            errorMsg = "Google sign-in canceled"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.account_border),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(1f)
                .fillMaxHeight(1f)
        )

        Image(
            painter = painterResource(id = R.drawable.back_button),
            contentDescription = "Cancel",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clickable { onBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp) // Removed spacing
        ) {
            Text(
                text = if (isSignUp) "Sign Up" else "Login",
                fontFamily = retroFontFamily,
                fontSize = 24.sp,
                color = Color.White
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )
            errorMsg?.let {
                Text(it, color = Color.Red)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp) // Removed spacing
            ) {
                ButtonWithImage(text = "Switch Sign Up/Login", onClick = { isSignUp = !isSignUp })
                ButtonWithImage(text = "Login/Sign Up", onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMsg = "Email/Password cannot be empty"
                    } else if (isSignUp) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoggedIn()
                                } else {
                                    errorMsg = task.exception?.localizedMessage ?: "Sign Up failed"
                                }
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoggedIn()
                                } else {
                                    errorMsg = task.exception?.localizedMessage ?: "Login failed"
                                }
                            }
                    }
                })
                ButtonWithImage(text = "Sign in with Google", onClick = {
                    errorMsg = null
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }
                })
            }
        }
    }
}

@Composable
fun ButtonWithImage(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.account_button),
            contentDescription = text,
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        Text(text = text, color = Color.White)
    }
}


