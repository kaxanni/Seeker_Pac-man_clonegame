package com.example.pacmanclone.game

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
import com.example.pacmanclone.util.MusicController
import com.example.pacmanclone.util.updateUserHighScores
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val retroFontFamily = FontFamily(Font(R.font.retro_font))

enum class GameStatus {
    RUNNING
}

@Composable
fun HeartIcon() {
    Icon(
        painter = painterResource(R.drawable.heart),
        contentDescription = "Life",
        tint = Color.Red,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun PacmanGame(onBack: () -> Unit) {
    var mazeState by remember { mutableStateOf(createOriginalMaze()) }
    val rows = mazeState.size
    val cols = mazeState[0].size

    // Ghost(s) with AI logic from Ghost.kt
    var ghosts by remember { mutableStateOf(listOf(Ghost(Pair(11, 13)))) }

    // Pac-Man
    var pacmanPos by remember { mutableStateOf(Pair(17, 13)) }
    var score by remember { mutableIntStateOf(0) }
    var gameStatus by remember { mutableStateOf(GameStatus.RUNNING) }
    var pacmanDirection by remember { mutableStateOf(Direction.NONE) }

    // Pellets
    var pelletCount by remember { mutableIntStateOf(0) }

    // Invulnerability
    var isInvulnerable by remember { mutableStateOf(false) }
    var invulnerabilityTimer by remember { mutableLongStateOf(0L) }

    // Speed Boost
    var isSpeedBoost by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Lives & Game Over
    var pacmanLives by remember { mutableStateOf(3) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    // Count how many times the maze was cleared
    var timesCleared by remember { mutableStateOf(0) }





    // Coin image
    val context = LocalContext.current
    val coinImageBitmap = remember {
        ImageBitmap.imageResource(context.resources, R.drawable.gold_coin)
    }

    // Create the MediaPlayer and start music
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music) // replace with your mp3 name
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

    // Use solo_background.jpeg for single player
    val backgroundPainter = painterResource(R.drawable.solo_background)

    // Helper to check if the maze is empty
    fun isMazeEmpty(maze: List<List<Int>>): Boolean {
        return maze.flatten().none { it == PELLET }
    }

    // Main game loop
    LaunchedEffect(gameStatus) {
        while (gameStatus == GameStatus.RUNNING) {
            // 1) Delay depends on speed boost
            val stepDelay = if (isSpeedBoost) 150L else 300L
            delay(stepDelay)

            // 2) If not speed boosted, an optional extra delay
            if (!isSpeedBoost) delay(300L)

            // 3) Pac-Man moves steps times if speed boost
            val steps = if (isSpeedBoost) 2 else 1
            repeat(steps) {
                val nextPos = getNextPosition(pacmanPos, pacmanDirection)
                if (
                    nextPos.first in 0 until rows &&
                    nextPos.second in 0 until cols &&
                    mazeState[nextPos.first][nextPos.second] != WALL
                ) {
                    pacmanPos = nextPos
                    if (mazeState[pacmanPos.first][pacmanPos.second] == PELLET) {
                        mazeState = mazeState.map { it.toMutableList() }
                            .also { it[pacmanPos.first][pacmanPos.second] = EMPTY }
                        score += 10
                        pelletCount += 1
                    }
                }
            }

            // 4) Ghost(s) move exactly once using AI from Ghost.kt
            ghosts = ghosts.map { ghost ->
                ghost.move(mazeState, pacmanPos)
            }

            // 5) Collision check
            for (ghost in ghosts) {
                if (pacmanPos == ghost.position) {
                    if (isInvulnerable) {
                        // Pac-Man kills ghost
                        ghosts = ghosts.map {
                            if (it == ghost) it.copy(position = Pair(11, 13)) else it
                        }
                        score += 50
                    } else {
                        // Pac-Man loses a life
                        pacmanLives -= 1
                        if (pacmanLives > 0) {
                            pacmanPos = Pair(17, 13)
                            ghosts = ghosts.map { it.copy(position = Pair(11, 13)) }
                            pacmanDirection = Direction.NONE
                        } else {
                            updateUserHighScores(score)
                            showGameOverDialog = true
                        }
                    }
                }
            }

            // 6) If all pellets are eaten
            if (pacmanLives > 0 && isMazeEmpty(mazeState)) {
                timesCleared++
                mazeState = createOriginalMaze()
                pacmanPos = Pair(17, 13)
                ghosts = ghosts.map { it.copy(position = Pair(11, 13)) }
                if (timesCleared % 3 == 0 && ghosts.size < 4) {
                    ghosts = ghosts + Ghost(Pair(11, 13))
                }
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

    // UI Layout with solo background
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = backgroundPainter,
            contentDescription = "Solo Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.fillMaxSize()) {
            // Top row: Back button & hearts
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
                        text = "Single-Player",
                        color = Color.White,
                        fontFamily = retroFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    repeat(pacmanLives) {
                        HeartIcon()
                    }
                }
            }
            // Main game area
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // DPad
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DPad { direction ->
                        pacmanDirection = direction
                    }
                }
                // Maze
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val boardSize = if (maxWidth < maxHeight) maxWidth else maxHeight
                    Box(
                        modifier = Modifier
                            .size(boardSize)
                            .background(Color.Black)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellWidth = size.width / cols
                            val cellHeight = size.height / rows
                            drawRoundRect(
                                color = Color.Blue,
                                topLeft = Offset(0f, 0f),
                                size = size,
                                style = Stroke(width = 5f),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            val wallStrokeWidth = 4f
                            for (i in 0 until rows) {
                                for (j in 0 until cols) {
                                    if (mazeState[i][j] == WALL) {
                                        val left = j * cellWidth
                                        val top = i * cellHeight
                                        drawRoundRect(
                                            color = Color.Blue,
                                            topLeft = Offset(left, top),
                                            size = Size(cellWidth, cellHeight),
                                            style = Stroke(width = wallStrokeWidth),
                                            cornerRadius = CornerRadius(6f, 6f)
                                        )
                                    }
                                }
                            }
                            val coinSizeFactor = 0.8f
                            for (i in 0 until rows) {
                                for (j in 0 until cols) {
                                    if (mazeState[i][j] == PELLET) {
                                        val left = j * cellWidth
                                        val top = i * cellHeight
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
                                }
                            }
                            val pacColor = if (isInvulnerable) Color.Green else Color.White
                            val pacLeft = pacmanPos.second * cellWidth
                            val pacTop = pacmanPos.first * cellHeight
                            drawArc(
                                color = pacColor,
                                startAngle = 30f,
                                sweepAngle = 300f,
                                useCenter = true,
                                topLeft = Offset(pacLeft, pacTop),
                                size = Size(cellWidth, cellHeight)
                            )
                            for (ghost in ghosts) {
                                val ghostX = ghost.position.second * cellWidth + cellWidth / 2
                                val ghostY = ghost.position.first * cellHeight + cellHeight / 2
                                drawCircle(
                                    color = Color.Red,
                                    radius = (cellWidth.coerceAtMost(cellHeight)) * 0.4f,
                                    center = Offset(ghostX, ghostY)
                                )
                            }
                        }
                    }
                }
                // Right side: Score, pellet count, power-ups
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Score: $score", color = Color.Red, fontFamily = retroFontFamily)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pellets: $pelletCount", color = Color.Red, fontFamily = retroFontFamily)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Invulnerability
                    if (!isInvulnerable && pelletCount >= 50) {
                        Button(
                            onClick = {
                                pelletCount -= 50
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
                                tint = Color.Yellow,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    // Speed Boost
                    if (!isSpeedBoost && pelletCount >= 20) {
                        Button(
                            onClick = {
                                pelletCount -= 20
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
                }
            }
        }
    }

    // Game Over dialog
    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game Over", color = Color.Red, fontFamily = retroFontFamily) },
            text = { Text("Final Score: $score", fontFamily = retroFontFamily) },
            confirmButton = {
                Button(onClick = {
                    showGameOverDialog = false
                    // Reset the game
                    mazeState = createOriginalMaze()
                    pacmanPos = Pair(17, 13)
                    ghosts = listOf(Ghost(Pair(11, 13)))
                    pacmanLives = 3
                    gameStatus = GameStatus.RUNNING
                    pacmanDirection = Direction.NONE
                    pelletCount = 0
                }) {
                    Text("Restart", fontFamily = retroFontFamily)
                }
            },
            dismissButton = {
                Button(onClick = onBack) {
                    Text("Quit", fontFamily = retroFontFamily)
                }
            },
            shape = RoundedCornerShape(10.dp)
        )
    }
}
