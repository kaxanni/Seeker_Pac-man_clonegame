@file:Suppress("DEPRECATION")

package com.example.pacmanclone.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pacmanclone.R
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

    // Retrieve the Web Client ID from strings.xml
    val webClientId = stringResource(R.string.default_web_client_id)

    // Configure Google Sign-In Options using the webClientId
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(gso) {
        GoogleSignIn.getClient(context, gso)
    }

    // Create an ActivityResultLauncher to handle the sign-in intent result
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isSignUp) "Sign Up" else "Login")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Spacer(Modifier.height(8.dp))
        errorMsg?.let {
            Text(it, color = Color.Red)
            Spacer(Modifier.height(8.dp))
        }
        Row {
            Button(onClick = { isSignUp = !isSignUp }) {
                Text(if (isSignUp) "Switch to Login" else "Switch to Sign Up")
            }
            Spacer(Modifier.width(16.dp))
            Button(onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMsg = "Email/Password cannot be empty"
                    return@Button
                }
                if (isSignUp) {
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
            }) {
                Text(if (isSignUp) "Sign Up" else "Login")
            }
        }
        Spacer(Modifier.height(16.dp))
        // Google Sign-In button: sign out first to force account chooser
        Button(onClick = {
            errorMsg = null
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }) {
            Text("Sign in with Google")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Cancel")
        }
    }
}
