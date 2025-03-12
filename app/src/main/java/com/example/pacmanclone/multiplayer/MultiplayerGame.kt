package com.example.pacmanclone.multiplayer

import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import com.example.pacmanclone.R
import com.example.pacmanclone.game.*
import com.example.pacmanclone.util.MusicController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val retroFontFamily = FontFamily(Font(R.font.retro_font))

@Composable
fun MultiplayerGame(
    match: MultiplayerMatch,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val gameDocRef = db.collection("multiplayerMatches").document(match.matchId)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isPacman = (currentUser?.uid == match.pacmanUid)

    // Maze for collision checks
    val wallOnlyMaze = remember { createWallOnlyMaze() }

    // Firestore states
    var pacmanPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var ghostPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var pacmanLives by remember { mutableStateOf<Int?>(null) }
    var pelletCount by remember { mutableStateOf<Int?>(null) }
    var gameOver by remember { mutableStateOf<Boolean?>(null) }
    var pellets by remember { mutableStateOf<List<String>?>(null) }
    var ghostInvertedControl by remember { mutableStateOf(false) }

    // Local states
    var pacmanDirection by remember { mutableStateOf(Direction.NONE) }
    var ghostDirection by remember { mutableStateOf(Direction.NONE) }

    // Pac-Man power-ups
    var isInvulnerable by remember { mutableStateOf(false) }
    var invulnerabilityTimer by remember { mutableLongStateOf(0L) }
    var isSpeedBoost by remember { mutableStateOf(false) }

    // Ghost power-ups (cooldown)
    var ghostSpeedBoost by remember { mutableStateOf(false) }
    var ghostSpeedLastUsed by remember { mutableStateOf(0L) }
    var ghostInvertLastUsed by remember { mutableStateOf(0L) }

    var opponentName by remember { mutableStateOf("Loading...") }
    val context = LocalContext.current
    val coinImageBitmap = remember {
        ImageBitmap.imageResource(context.resources, R.drawable.gold_coin)
    }
    val backgroundPainter = painterResource(R.drawable.multi_background)
    var loaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Music
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music)
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

    // Firestore listener
    DisposableEffect(match.matchId) {
        val listener = gameDocRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                snapshot.get("pacmanPos")?.let {
                    val list = it as List<*>
                    pacmanPos = Pair((list[0] as Long).toInt(), (list[1] as Long).toInt())
                }
                snapshot.get("ghostPos")?.let {
                    val list = it as List<*>
                    ghostPos = Pair((list[0] as Long).toInt(), (list[1] as Long).toInt())
                }
                snapshot.getLong("pacmanLives")?.let { pacmanLives = it.toInt() }
                snapshot.getLong("pelletCount")?.let { pelletCount = it.toInt() }
                snapshot.getBoolean("gameOver")?.let { gameOver = it }
                snapshot.get("pellets")?.let {
                    pellets = (it as List<*>).filterIsInstance<String>()
                }
                ghostInvertedControl = snapshot.getBoolean("ghostInvertedControl") ?: false

                if (
                    pacmanPos != null &&
                    ghostPos != null &&
                    pacmanLives != null &&
                    pelletCount != null &&
                    gameOver != null &&
                    pellets != null
                ) {
                    loaded = true
                }
            }
        }
        onDispose { listener.remove() }
    }

    // Load opponent name
    LaunchedEffect(isPacman) {
        val opponentUid = if (isPacman) match.ghostUid else match.pacmanUid
        if (opponentUid.isNotEmpty()) {
            db.collection("users").document(opponentUid).get()
                .addOnSuccessListener { doc ->
                    opponentName = doc.getString("username") ?: "Unknown"
                }
        }
    }

    // Helper to update doc
    fun updateGameState(
        newPacmanPos: Pair<Int, Int>? = null,
        newGhostPos: Pair<Int, Int>? = null,
        newPellets: List<String>? = null,
        newPelletCount: Int? = null,
        newPacmanLives: Int? = null,
        newGameOver: Boolean? = null,
        newGhostInvertedControl: Boolean? = null
    ) {
        val updates = mutableMapOf<String, Any>()
        newPacmanPos?.let { updates["pacmanPos"] = listOf(it.first, it.second) }
        newGhostPos?.let { updates["ghostPos"] = listOf(it.first, it.second) }
        newPellets?.let { updates["pellets"] = it }
        newPelletCount?.let { updates["pelletCount"] = it }
        newPacmanLives?.let { updates["pacmanLives"] = it }
        newGameOver?.let { updates["gameOver"] = it }
        newGhostInvertedControl?.let { updates["ghostInvertedControl"] = it }
        if (updates.isNotEmpty()) {
            gameDocRef.update(updates)
        }
    }

    fun recordMultiplayerWin(winnerUid: String) {
        val userDoc = db.collection("users").document(winnerUid)
        userDoc.update("multiplayerWins", FieldValue.increment(1))
    }

    // Main game loop
    LaunchedEffect(loaded) {
        if (!loaded) return@LaunchedEffect
        while (gameOver == false) {
            val delayTime = if (isPacman) {
                if (isSpeedBoost) 150L else 300L
            } else {
                if (ghostSpeedBoost) 200L else 300L
            }
            delay(delayTime)

            if (!loaded) break
            if (pacmanPos == null || ghostPos == null || pacmanLives == null || pelletCount == null || gameOver == null || pellets == null)
                break

            if (isPacman) {
                val steps = if (isSpeedBoost) 2 else 1
                repeat(steps) {
                    val nextPos = getNextPosition(pacmanPos!!, pacmanDirection)
                    if (isValidMove(wallOnlyMaze, nextPos)) {
                        val pelletKey = "${nextPos.first},${nextPos.second}"
                        if (pellets!!.contains(pelletKey)) {
                            val newPellets = pellets!!.toMutableList()
                            newPellets.remove(pelletKey)
                            updateGameState(
                                newPacmanPos = nextPos,
                                newPellets = newPellets,
                                newPelletCount = pelletCount!! + 1
                            )
                        } else {
                            updateGameState(newPacmanPos = nextPos)
                        }
                    }
                }
            } else {
                val steps = if (ghostSpeedBoost) 2 else 1
                repeat(steps) {
                    val nextPos = getNextPosition(ghostPos!!, ghostDirection)
                    if (isValidMove(wallOnlyMaze, nextPos)) {
                        updateGameState(newGhostPos = nextPos)
                    }
                }
            }

            // *** Updated collision logic: only Pac-Man's device changes gameOver or lives. ***
            if (pacmanPos == ghostPos) {
                // We do collision logic only from Pac-Man's perspective
                if (isPacman) {
                    if (!isInvulnerable) {
                        val newLives = pacmanLives!! - 1
                        if (newLives > 0) {
                            // Pac-Man still has lives => reset position
                            updateGameState(
                                newPacmanPos = Pair(17, 13),
                                newPacmanLives = newLives
                            )
                        } else {
                            // Pac-Man died => game over => ghost wins
                            updateGameState(newGameOver = true)
                            recordMultiplayerWin(match.ghostUid)
                        }
                    } else {
                        // Pac-Man is invulnerable => ghost is reset
                        updateGameState(newGhostPos = Pair(5, 13))
                    }
                }
                // If the user is ghost, do NOTHING here
                // => Pac-Man's device will handle the collision in its own loop
            }

            // If all pellets are eaten => Pac-Man wins
            if (isPacman && pellets!!.isEmpty()) {
                updateGameState(newGameOver = true)
                recordMultiplayerWin(match.pacmanUid)
            }
        }
    }

    // Invulnerability timer
    LaunchedEffect(invulnerabilityTimer) {
        if (invulnerabilityTimer > 0L) {
            delay(5000L)
            isInvulnerable = false
            invulnerabilityTimer = 0L
        }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = backgroundPainter,
            contentDescription = "Multiplayer Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.fillMaxSize()) {
            // Top row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.back_button),
                        contentDescription = "Back",
                        tint = Color.Unspecified
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPacman) "You are Pac-Man" else "You are Ghost",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontFamily = retroFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (pacmanLives != null) {
                        repeat(pacmanLives!!) {
                            Icon(
                                painter = painterResource(R.drawable.heart),
                                contentDescription = "Pac-Man Heart",
                                tint = if (isPacman) Color.Red else Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
                Text(
                    text = "Opponent: $opponentName",
                    fontSize = 14.sp,
                    color = Color.Red,
                    fontFamily = retroFontFamily
                )
            }

            if (!loaded) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading game data...", color = Color.White, fontFamily = retroFontFamily)
                }
            } else {
                val pPos = pacmanPos!!
                val gPos = ghostPos!!
                val pCount = pelletCount!!
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left column (DPad)
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        DPad { direction ->
                            if (isPacman) {
                                pacmanDirection = if (ghostInvertedControl) invertDirection(direction) else direction
                            } else {
                                ghostDirection = direction
                            }
                        }
                        if (!isPacman) {
                            Text(
                                "Ghost Dir: $ghostDirection",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = retroFontFamily
                            )
                        }
                    }
                    // Maze in the center
                    BoxWithConstraints(
                        modifier = Modifier.weight(2f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val boardSize = if (maxWidth < maxHeight) maxWidth else maxHeight
                        Box(modifier = Modifier.size(boardSize).background(Color.Black)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val rows = wallOnlyMaze.size
                                val cols = wallOnlyMaze[0].size
                                val cellWidth = size.width / cols
                                val cellHeight = size.height / rows
                                drawRoundRect(
                                    color = Color.Blue,
                                    topLeft = Offset(0f, 0f),
                                    size = size,
                                    style = Stroke(width = 5f),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )
                                for (r in 0 until rows) {
                                    for (c in 0 until cols) {
                                        if (wallOnlyMaze[r][c] == WALL) {
                                            val left = c * cellWidth
                                            val top = r * cellHeight
                                            drawRoundRect(
                                                color = Color.Blue,
                                                topLeft = Offset(left, top),
                                                size = Size(cellWidth, cellHeight),
                                                style = Stroke(width = 4f),
                                                cornerRadius = CornerRadius(6f, 6f)
                                            )
                                        }
                                    }
                                }
                                pellets?.forEach { pellet ->
                                    val (pr, pc) = pellet.split(",").map { it.toInt() }
                                    val left = pc * cellWidth
                                    val top = pr * cellHeight
                                    val coinSizeFactor = 0.8f
                                    val coinSize = (cellWidth.coerceAtMost(cellHeight)) * coinSizeFactor
                                    val offsetLeft = left + (cellWidth - coinSize) / 2f
                                    val offsetTop = top + (cellHeight - coinSize) / 2f
                                    drawIntoCanvas { canvas ->
                                        val paint = android.graphics.Paint()
                                        val srcRect = Rect(0, 0, coinImageBitmap.width, coinImageBitmap.height)
                                        val dstRect = RectF(
                                            offsetLeft,
                                            offsetTop,
                                            offsetLeft + coinSize,
                                            offsetTop + coinSize
                                        )
                                        canvas.nativeCanvas.drawBitmap(
                                            coinImageBitmap.asAndroidBitmap(),
                                            srcRect,
                                            dstRect,
                                            paint
                                        )
                                    }
                                }
                                val pacColor = when {
                                    ghostInvertedControl && isPacman -> Color.Blue
                                    isInvulnerable -> Color.Green
                                    else -> Color.White
                                }
                                val pacLeft = pPos.second * cellWidth
                                val pacTop = pPos.first * cellHeight
                                drawArc(
                                    color = pacColor,
                                    startAngle = 30f,
                                    sweepAngle = 300f,
                                    useCenter = true,
                                    topLeft = Offset(pacLeft, pacTop),
                                    size = Size(cellWidth, cellHeight)
                                )
                                val ghostLeft = gPos.second * cellWidth
                                val ghostTop = gPos.first * cellHeight
                                drawCircle(
                                    color = Color.Red,
                                    radius = (cellWidth.coerceAtMost(cellHeight)) * 0.4f,
                                    center = Offset(ghostLeft + cellWidth / 2, ghostTop + cellHeight / 2)
                                )
                            }
                        }
                    }
                    // Right column (power-ups)
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isPacman) {
                            if (!isInvulnerable && pCount >= 50) {
                                Button(
                                    onClick = {
                                        updateGameState(newPelletCount = pCount - 50)
                                        isInvulnerable = true
                                        invulnerabilityTimer = System.currentTimeMillis()
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(64.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shield),
                                        contentDescription = "Invulnerability Shield",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            if (!isSpeedBoost && pCount >= 20) {
                                Button(
                                    onClick = {
                                        updateGameState(newPelletCount = pCount - 20)
                                        isSpeedBoost = true
                                        scope.launch {
                                            delay(5000L)
                                            isSpeedBoost = false
                                        }
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(64.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.speed_boost),
                                        contentDescription = "Speed Boost",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        } else {
                            val now = System.currentTimeMillis()
                            val canUseSpeed = (now - ghostSpeedLastUsed) >= 7000
                            Button(
                                onClick = {
                                    if (canUseSpeed && !ghostSpeedBoost) {
                                        ghostSpeedBoost = true
                                        ghostSpeedLastUsed = System.currentTimeMillis()
                                        scope.launch {
                                            delay(5000L)
                                            ghostSpeedBoost = false
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                enabled = canUseSpeed && !ghostSpeedBoost
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.speed_boost),
                                    contentDescription = "Ghost Speed Boost",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            val canUseInvert = (now - ghostInvertLastUsed) >= 10000
                            Button(
                                onClick = {
                                    if (canUseInvert && !ghostInvertedControl) {
                                        ghostInvertedControl = true
                                        updateGameState(newGhostInvertedControl = true)
                                        ghostInvertLastUsed = System.currentTimeMillis()
                                        scope.launch {
                                            delay(5000L)
                                            ghostInvertedControl = false
                                            updateGameState(newGhostInvertedControl = false)
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                enabled = canUseInvert && !ghostInvertedControl
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.invert_control),
                                    contentDescription = "Invert Control",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
            if (gameOver == true) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Game Over", color = Color.Red, fontFamily = retroFontFamily) },
                    text = { Text("Thanks for playing!", fontFamily = retroFontFamily) },
                    confirmButton = {
                        Button(onClick = onBack) {
                            Text("Quit", fontFamily = retroFontFamily)
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

fun createWallOnlyMaze(): List<List<Int>> {
    val fullMaze = createOriginalMaze()
    return fullMaze.map { row ->
        row.map { cell ->
            if (cell == PELLET) EMPTY else cell
        }
    }
}

fun isValidMove(maze: List<List<Int>>, pos: Pair<Int, Int>): Boolean {
    val (r, c) = pos
    if (r < 0 || r >= maze.size || c < 0 || c >= maze[0].size) return false
    return (maze[r][c] != WALL)
}

fun invertDirection(direction: Direction): Direction {
    return when (direction) {
        Direction.UP -> Direction.DOWN
        Direction.DOWN -> Direction.UP
        Direction.LEFT -> Direction.RIGHT
        Direction.RIGHT -> Direction.LEFT
        Direction.NONE -> Direction.NONE
    }
}
