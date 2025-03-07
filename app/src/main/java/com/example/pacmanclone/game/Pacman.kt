package com.example.pacmanclone.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.graphics.nativeCanvas
import com.example.pacmanclone.R

@Composable
fun PacmanGame(onBack: () -> Unit) {
    // Maze & Game States
    var mazeState by remember { mutableStateOf(createOriginalMaze()) }
    val rows = mazeState.size
    val cols = mazeState[0].size

    var pacmanPos by remember { mutableStateOf(Pair(17, 13)) }
    var ghost by remember { mutableStateOf(Ghost(Pair(11, 13))) }
    var score by remember { mutableStateOf(0) }
    var gameStatus by remember { mutableStateOf(GameStatus.RUNNING) }
    var pacmanDirection by remember { mutableStateOf(Direction.NONE) }

    // Pellet logic
    var pelletCount by remember { mutableStateOf(0) }

    // Invulnerability logic
    var isInvulnerable by remember { mutableStateOf(false) }
    var invulnerabilityTimer by remember { mutableStateOf(0L) }

    // Lives & Game Over
    var pacmanLives by remember { mutableStateOf(3) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    // Load gold_coin.png once
    val context = LocalContext.current
    val coinImageBitmap = remember {
        androidx.compose.ui.graphics.ImageBitmap.imageResource(
            context.resources,
            R.drawable.gold_coin
        )
    }

    // Game loop
    LaunchedEffect(gameStatus) {
        while (gameStatus == GameStatus.RUNNING) {
            delay(300L)
            val nextPos = getNextPosition(pacmanPos, pacmanDirection)
            if (nextPos.first in 0 until rows &&
                nextPos.second in 0 until cols &&
                mazeState[nextPos.first][nextPos.second] != WALL
            ) {
                pacmanPos = nextPos
                // If Pac-Man lands on a pellet
                if (mazeState[pacmanPos.first][pacmanPos.second] == PELLET) {
                    // Clear pellet
                    mazeState = mazeState.map { it.toMutableList() }
                        .also { it[pacmanPos.first][pacmanPos.second] = EMPTY }
                    score += 10
                    pelletCount += 1
                }
            }
            // Ghost moves
            ghost = ghost.move(mazeState, pacmanPos)
            // Collision check
            if (pacmanPos == ghost.position) {
                if (isInvulnerable) {
                    ghost = Ghost(Pair(11, 13))
                    score += 50
                } else {
                    pacmanLives -= 1
                    if (pacmanLives > 0) {
                        pacmanPos = Pair(17, 13)
                        ghost = Ghost(Pair(11, 13))
                        pacmanDirection = Direction.NONE
                    } else {
                        showGameOverDialog = true
                    }
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

    // Main UI
    Column(modifier = Modifier.fillMaxSize()) {
        // Top row: Back button & hearts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use IconButton with back_button.png
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.back_button),
                    contentDescription = "Back",
                    tint = Color.Unspecified
                )
            }
            // Display hearts
            Row {
                repeat(pacmanLives) {
                    HeartIcon()
                }
            }
        }

        // Main layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: DPad
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DPad { direction ->
                    pacmanDirection = direction
                }
            }

            // CENTER: Maze
            BoxWithConstraints(
                modifier = Modifier.weight(2f).fillMaxHeight(),
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

                        // Outer border
                        drawRoundRect(
                            color = Color.Blue,
                            topLeft = Offset(0f, 0f),
                            size = size,
                            style = Stroke(width = 5f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )

                        // Draw walls
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

                        // Draw gold coin pellets at ~80% of min(cellWidth, cellHeight)
                        val coinSizeFactor = 0.8f // diameter ~ 40% of cell dimension
                        for (i in 0 until rows) {
                            for (j in 0 until cols) {
                                if (mazeState[i][j] == PELLET) {
                                    val left = j * cellWidth
                                    val top = i * cellHeight

                                    val coinSize = (cellWidth.coerceAtMost(cellHeight)) * coinSizeFactor
                                    val offsetLeft = left + (cellWidth - coinSize) / 2f
                                    val offsetTop = top + (cellHeight - coinSize) / 2f

                                    // Draw the coin via nativeCanvas
                                    drawIntoCanvas { canvas ->
                                        val paint = android.graphics.Paint()
                                        val srcRect = Rect(
                                            0,
                                            0,
                                            coinImageBitmap.width,
                                            coinImageBitmap.height
                                        )
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

                        // Pac-Man
                        val pacLeft = pacmanPos.second * cellWidth
                        val pacTop = pacmanPos.first * cellHeight
                        drawArc(
                            color = if (isInvulnerable) Color.Green else Color.Yellow,
                            startAngle = 30f,
                            sweepAngle = 300f,
                            useCenter = true,
                            topLeft = Offset(pacLeft, pacTop),
                            size = Size(cellWidth, cellHeight)
                        )

                        // Ghost
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

            // RIGHT: Score, Pellets, & Invulnerability
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Score: $score", color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Pellets: $pelletCount", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))

                // Circle button with shield if pelletCount >= 50
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
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }
            }
        }
    }

    // Game Over pop-up
    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game Over", color = Color.Red) },
            text = { Text("Final Score: $score") },
            confirmButton = {
                Button(onClick = {
                    showGameOverDialog = false
                    mazeState = createOriginalMaze()
                    pacmanPos = Pair(17, 13)
                    ghost = Ghost(Pair(11, 13))
                    score = 0
                    pacmanLives = 3
                    gameStatus = GameStatus.RUNNING
                    pacmanDirection = Direction.NONE
                    pelletCount = 0
                }) {
                    Text("Restart")
                }
            },
            dismissButton = {
                Button(onClick = onBack) {
                    Text("Quit")
                }
            },
            shape = RoundedCornerShape(10.dp)
        )
    }
}

/**
 * HeartIcon() - shows heart.png as a small image
 */
@Composable
fun HeartIcon() {
    Image(
        painter = painterResource(R.drawable.heart),
        contentDescription = "Heart",
        modifier = Modifier.size(24.dp)
    )
}
