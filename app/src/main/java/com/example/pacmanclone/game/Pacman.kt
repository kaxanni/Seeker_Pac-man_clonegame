package com.example.pacmanclone.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PacmanGame(onBack: () -> Unit) {
    var mazeState by remember { mutableStateOf(createOriginalMaze()) }
    val rows = mazeState.size
    val cols = mazeState[0].size

    var pacmanPos by remember { mutableStateOf(Pair(17, 13)) }
    var ghost by remember { mutableStateOf(Ghost(Pair(11, 13))) }
    var score by remember { mutableIntStateOf(0) }
    var gameStatus by remember { mutableStateOf(GameStatus.RUNNING) }
    var pacmanDirection by remember { mutableStateOf(Direction.NONE) }

    var pelletCount by remember { mutableIntStateOf(0) }
    var isInvulnerable by remember { mutableStateOf(false) }
    var invulnerabilityTimer by remember { mutableLongStateOf(0L) }

    // 🔴 Pac-Man has 3 lives
    var pacmanLives by remember { mutableIntStateOf(3) }

    LaunchedEffect(gameStatus) {
        while (gameStatus == GameStatus.RUNNING) {
            delay(300L)
            val nextPacmanPos = getNextPosition(pacmanPos, pacmanDirection)
            if (nextPacmanPos.first in 0 until rows &&
                nextPacmanPos.second in 0 until cols &&
                mazeState[nextPacmanPos.first][nextPacmanPos.second] != WALL
            ) {
                pacmanPos = nextPacmanPos
                when (mazeState[pacmanPos.first][pacmanPos.second]) {
                    PELLET -> {
                        mazeState = mazeState.map { it.toMutableList() }
                            .also { it[pacmanPos.first][pacmanPos.second] = EMPTY }
                        score += 10
                        pelletCount += 1
                    }
                }
            }
            ghost = ghost.move(mazeState, pacmanPos)

            // 🔴 Collision Handling
            if (pacmanPos == ghost.position) {
                if (isInvulnerable) {
                    ghost = Ghost(Pair(11, 13))
                    score += 50
                } else {
                    pacmanLives -= 1  // Lose a life
                    if (pacmanLives > 0) {
                        // Respawn Pac-Man
                        pacmanPos = Pair(17, 13)
                        ghost = Ghost(Pair(11, 13))
                        pacmanDirection = Direction.NONE
                    } else {
                        // Game Over when all lives are lost
                        gameStatus = GameStatus.GAME_OVER
                    }
                }
            }
        }
    }

    LaunchedEffect(invulnerabilityTimer) {
        if (invulnerabilityTimer > 0L) {
            delay(5000L)
            isInvulnerable = false
            invulnerabilityTimer = 0L
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Back button at the top-left
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
        }

        if (gameStatus == GameStatus.GAME_OVER) {
            // 🎮 Game Over Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "GAME OVER", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Final Score: $score", color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = {
                        // Restart Game
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

                    Button(onClick = {
                        // Quit to Main Menu
                        onBack()
                    }) {
                        Text("Quit")
                    }
                }
            }
        } else {
            // 🕹️ Main Game Layout (When game is still running)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT SIDE - D-Pad Controller
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DPad { direction ->
                        pacmanDirection = direction
                    }
                }

                // CENTER - The Game Maze
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

                            // 🟦 Draw Outer Border
                            drawRoundRect(
                                color = Color.Blue,
                                topLeft = Offset(0f, 0f),
                                size = size,
                                style = Stroke(width = 5f),
                                cornerRadius = CornerRadius(10f, 10f)
                            )

                            // 🔷 Draw Walls
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

                            // 🔵 Draw Pellets
                            for (i in 0 until rows) {
                                for (j in 0 until cols) {
                                    if (mazeState[i][j] == PELLET) {
                                        val left = j * cellWidth
                                        val top = i * cellHeight
                                        drawCircle(
                                            color = Color.White,
                                            radius = (cellWidth.coerceAtMost(cellHeight)) * 0.2f,
                                            center = Offset(left + cellWidth / 2, top + cellHeight / 2)
                                        )
                                    }
                                }
                            }

                            // 🟡 Draw Pac-Man
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

                            // 🔴 Draw Ghost
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

                // RIGHT SIDE - Score, Lives, Invulnerability Button
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Score: $score", color = Color.Black)
                    Text(text = "Lives: $pacmanLives", color = Color.Red)
                }
            }
        }
    }
}
