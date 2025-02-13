package com.example.pacmanclone.game

// Constants representing cell types.
const val EMPTY = 0
const val WALL = 1
const val PELLET = 2

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
 * The maze is defined using a list of strings.
 * Each character represents a tile:
 * - '#' → wall
 * - '.' → pellet
 * - ' ' → empty space (used for the ghost house and corridors)
 */
fun createOriginalMaze(): List<MutableList<Int>> {
    val layout = listOf(
        "############################",
        "#............##............#",
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
    val cols = layout[0].length
    val maze = MutableList(rows) { MutableList(cols) { EMPTY } }
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            maze[i][j] = when (layout[i][j]) {
                '#' -> WALL
                '.' -> PELLET
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