// PacmanGame.kt
package com.example.pacmanclone.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PacmanGame(onBack: () -> Unit) {
    // Initialize game state.
    var mazeState by remember { mutableStateOf(createOriginalMaze()) }
    val rows = mazeState.size
    val cols = mazeState[0].size

    // Set starting positions (adjust these as needed).
    var pacmanPos by remember { mutableStateOf(Pair(17, 13)) }
    var ghost by remember { mutableStateOf(Ghost(Pair(11, 13))) }

    var score by remember { mutableStateOf(0) }
    var gameStatus by remember { mutableStateOf(GameStatus.RUNNING) }
    var pacmanDirection by remember { mutableStateOf(Direction.NONE) }

    // Game loop: updates the game state every 300ms.
    LaunchedEffect(gameStatus) {
        while (gameStatus == GameStatus.RUNNING) {
            delay(300L)
            // Calculate the next position for Pac‑Man.
            val nextPacmanPos = getNextPosition(pacmanPos, pacmanDirection)
            if (nextPacmanPos.first in 0 until rows &&
                nextPacmanPos.second in 0 until cols &&
                mazeState[nextPacmanPos.first][nextPacmanPos.second] != WALL
            ) {
                pacmanPos = nextPacmanPos
                // If there is a pellet, "eat" it and update the score.
                if (mazeState[pacmanPos.first][pacmanPos.second] == PELLET) {
                    mazeState = mazeState.map { it.toMutableList() }
                        .also { it[pacmanPos.first][pacmanPos.second] = EMPTY }
                    score += 10
                }
            }
            // Update the ghost position, making it chase Pac‑Man.
            ghost = ghost.move(mazeState, pacmanPos)
            // End game if the ghost collides with Pac‑Man.
            if (pacmanPos == ghost.position) {
                gameStatus = GameStatus.GAME_OVER
            }
        }
    }

    // Main UI layout wrapped in a Column that includes a top back button.
    Column(modifier = Modifier.fillMaxSize()) {
        // Top row: Back button.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
        }

        // Game content.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Left side: Game board.
            BoxWithConstraints(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // Use the smaller of the available width or height to form a square board.
                val boardSize = if (maxWidth < maxHeight) maxWidth else maxHeight
                Box(
                    modifier = Modifier
                        .size(boardSize)
                        .background(Color.Black)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellWidth = size.width / cols
                        val cellHeight = size.height / rows

                        // 1. Draw the outer border.
                        drawRect(
                            color = Color.Blue,
                            topLeft = Offset(0f, 0f),
                            size = size,
                            style = Stroke(width = 3f)
                        )

                        // 2. Draw interior walls as thin blue lines.
                        val wallStrokeWidth = 1f
                        for (i in 1 until rows - 1) {
                            for (j in 1 until cols - 1) {
                                if (mazeState[i][j] == WALL) {
                                    val left = j * cellWidth
                                    val top = i * cellHeight
                                    if (mazeState[i - 1][j] != WALL) {
                                        drawLine(
                                            color = Color.Blue,
                                            start = Offset(left, top),
                                            end = Offset(left + cellWidth, top),
                                            strokeWidth = wallStrokeWidth
                                        )
                                    }
                                    if (mazeState[i + 1][j] != WALL) {
                                        drawLine(
                                            color = Color.Blue,
                                            start = Offset(left, top + cellHeight),
                                            end = Offset(left + cellWidth, top + cellHeight),
                                            strokeWidth = wallStrokeWidth
                                        )
                                    }
                                    if (mazeState[i][j - 1] != WALL) {
                                        drawLine(
                                            color = Color.Blue,
                                            start = Offset(left, top),
                                            end = Offset(left, top + cellHeight),
                                            strokeWidth = wallStrokeWidth
                                        )
                                    }
                                    if (mazeState[i][j + 1] != WALL) {
                                        drawLine(
                                            color = Color.Blue,
                                            start = Offset(left + cellWidth, top),
                                            end = Offset(left + cellWidth, top + cellHeight),
                                            strokeWidth = wallStrokeWidth
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Draw pellets.
                        for (i in 0 until rows) {
                            for (j in 0 until cols) {
                                if (mazeState[i][j] == PELLET) {
                                    val left = j * cellWidth
                                    val top = i * cellHeight
                                    drawCircle(
                                        color = Color.White,
                                        radius = (cellWidth.coerceAtMost(cellHeight)) * 0.1f,
                                        center = Offset(left + cellWidth / 2, top + cellHeight / 2)
                                    )
                                }
                            }
                        }

                        // 4. Draw Pac‑Man as a yellow arc.
                        val pacLeft = pacmanPos.second * cellWidth
                        val pacTop = pacmanPos.first * cellHeight
                        drawArc(
                            color = Color.Yellow,
                            startAngle = 30f,
                            sweepAngle = 300f,
                            useCenter = true,
                            topLeft = Offset(pacLeft, pacTop),
                            size = Size(cellWidth, cellHeight)
                        )

                        // 5. Draw the ghost as a red circle.
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

            Spacer(modifier = Modifier.width(16.dp))

            // Right side: D‑pad controller and score.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Score: $score", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                DPad { direction ->
                    pacmanDirection = direction
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (gameStatus == GameStatus.GAME_OVER) {
                    Button(onClick = {
                        // Restart the game.
                        mazeState = createOriginalMaze()
                        pacmanPos = Pair(17, 13)
                        ghost = Ghost(Pair(11, 13))
                        score = 0
                        gameStatus = GameStatus.RUNNING
                        pacmanDirection = Direction.NONE
                    }) {
                        Text("Restart")
                    }
                }
            }
        }
    }
}
