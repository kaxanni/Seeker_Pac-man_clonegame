package com.example.pacmanclone.menu

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacmanclone.R
import com.example.pacmanclone.multiplayer.retroFontFamily
import com.example.pacmanclone.util.MusicController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


@Composable
fun LeaderBoxContent(
    title: String,
    items: List<String>,
    centerItems: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(220.dp)
            .heightIn(min = 200.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.leaderboard_box),
            contentDescription = "Leaderboard Box Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = retroFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            val textAlignForItems = if (centerItems) TextAlign.Center else TextAlign.Left
            items.forEachIndexed { index, item ->
                Text(
                    text = "  ${index + 1}.$item",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = retroFontFamily,
                    textAlign = textAlignForItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MyHighScoresBox(modifier: Modifier = Modifier) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var scores by remember { mutableStateOf<List<Long>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val topScoresAny = snapshot.get("topScores") as? List<*> ?: emptyList<Any>()
                        val topScoresLong = topScoresAny.mapNotNull { it as? Long }
                        scores = topScoresLong
                    }
                }
        }
    }
    val scoreItems = scores.map { it.toString() }
    LeaderBoxContent(
        title = "My High Scores",
        items = scoreItems,
        centerItems = true,
        modifier = modifier
    )
}

@Composable
fun WorldHighScoresBox(modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    var worldScores by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .orderBy("bestScore", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val bestScore = doc.getLong("bestScore") ?: 0L
                    username to bestScore
                }
                worldScores = list
            }
    }
    val items = worldScores.map { "${it.first}  ${it.second}" }
    LeaderBoxContent(
        title = "World High Scores",
        items = items,
        centerItems = false,
        modifier = modifier
    )
}

@Composable
fun MultiplayerRankingBox(modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    var multiWins by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .orderBy("multiplayerWins", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val wins = doc.getLong("multiplayerWins") ?: 0L
                    username to wins
                }
                multiWins = list
            }
    }
    val items = multiWins.map { "${it.first}  ${it.second} wins" }
    LeaderBoxContent(
        title = "Multiplayer Ranking",
        items = items,
        centerItems = false,
        modifier = modifier
    )
}

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Create the MediaPlayer and start music
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.mainback_music) // replace with your mp3 name
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
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.leaderback),
            contentDescription = "Main Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.back_button),
                        contentDescription = "Back",
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyHighScoresBox(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                WorldHighScoresBox(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                MultiplayerRankingBox(modifier = Modifier.weight(1f))
            }
        }
    }
}
