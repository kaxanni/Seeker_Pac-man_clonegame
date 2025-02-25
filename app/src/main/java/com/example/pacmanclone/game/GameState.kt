package com.example.pacmanclone.game

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