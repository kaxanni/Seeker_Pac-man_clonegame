package com.example.pacmanclone.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pacmanclone.R
import com.example.pacmanclone.menu.retroFontFamily

// Constants representing cell types.
const val EMPTY = 0
const val WALL = 1
const val PELLET = 2
const val POWER_UP = 3  // (Not used in this mechanic)

// Available directions.
enum class Direction {
    UP, DOWN, LEFT, RIGHT, NONE
}

// Game status.
enum class GameStatus {
    RUNNING, GAME_OVER
}

/**
 * Creates the original Pac‑Man maze as a 2D grid.
 *
 * Each line MUST have exactly 28 columns.
 * '#' → wall
 * '.' → pellet
 * ' ' → empty space
 * 'P' → power‑up (not used in our new mechanic)
 */
fun createOriginalMaze(): List<MutableList<Int>> {
    val layout = listOf(
        "############################",
        "#...........##.............#",
        "#.####.#####.##.#####.####.#",
        "#.####.#####.##.#####.####.#",
        "#.####.#####.##.#####.####.#",
        "#..........................#",
        "#.####.##.########.##.####.#",
        "#.####.##.########.##.####.#",
        "#......##....##....##......#",
        "######.##### ## #####.######",
        "     #.##### ## #####.#     ",
        "     #.##          ##.#     ",
        "     #.## ######## ##.#     ",
        "######.## ######## ##.######",
        "#............##............#",
        "#.####.#####.##.#####.####.#",
        "#.####.#####.##.#####.####.#",
        "#...##................##...#",
        "###.##.##.########.##.##.###",
        "#......##....##....##......#",
        "#.##########.##.##########.#",
        "#.##########.##.##########.#",
        "#..........................#",
        "############################"
    )
    val rows = layout.size
    val cols = layout[0].length  // All lines should be 28 characters.
    val maze = MutableList(rows) { MutableList(cols) { EMPTY } }
    for (i in 0 until rows) {
        require(layout[i].length == cols) {
            "Line $i has length ${layout[i].length}, expected $cols."
        }
        for (j in 0 until cols) {
            maze[i][j] = when (layout[i][j]) {
                '#' -> WALL
                '.' -> PELLET
                'P' -> POWER_UP
                ' ' -> EMPTY
                else -> EMPTY
            }
        }
    }
    return maze
}

/**
 * Returns the next position from [pos] when moving in the given [direction].
 */
fun getNextPosition(pos: Pair<Int, Int>, direction: Direction): Pair<Int, Int> {
    return when (direction) {
        Direction.UP -> Pair(pos.first - 1, pos.second)
        Direction.DOWN -> Pair(pos.first + 1, pos.second)
        Direction.LEFT -> Pair(pos.first, pos.second - 1)
        Direction.RIGHT -> Pair(pos.first, pos.second + 1)
        Direction.NONE -> pos
    }
}
// Custom composable for a wooden-styled button
@Composable
fun WoodenButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(50.dp)
            .width(180.dp)
            .clickable(onClick = onClick), // Button click action
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.wooden_button), // Background image for button
            contentDescription = "Button Background",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = text,
            color = androidx.compose.ui.graphics.Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = retroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp, // Adjusted text size
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}