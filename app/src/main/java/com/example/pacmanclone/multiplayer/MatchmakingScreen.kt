package com.example.pacmanclone.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacmanclone.R
import kotlinx.coroutines.launch

@Composable
fun MatchmakingScreen(
    selectedRole: String,
    onMatchFound: (MultiplayerMatch?) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("Searching for a match...") }

    LaunchedEffect(Unit) {
        scope.launch {
            val match = joinMatchmaking(selectedRole)
            if (match != null) {
                onMatchFound(match)
            } else {
                statusText = "No match found. Please try again."
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        Text(
            text = statusText,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.back_button),
                contentDescription = "Back",
                tint = Color.Unspecified
            )
        }
    }
}
