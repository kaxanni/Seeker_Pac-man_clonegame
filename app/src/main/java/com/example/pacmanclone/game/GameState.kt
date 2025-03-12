package com.example.pacmanclone.game

const val EMPTY = 0
const val WALL = 1
const val PELLET = 2

enum class Direction {
    UP, DOWN, LEFT, RIGHT, NONE
}

fun createOriginalMaze(): List<MutableList<Int>> {
    val layout = listOf(
        "############################",
        "#..........................#",
        "#.####.#####.##.#####.####.#",
        "#.####.#####.##.#####.####.#",
        "#.####.#####.##.#####.####.#",
        "#..........................#",
        "#.####.##.########.##.####.#",
        "#.####.##.########.##.####.#",
        "#......##....##....##......#",
        "######.#####.##.#####.######",
        "     #.#####.##.#####.#     ",
        "     #.##..........##.#     ",
        "     #.##.########.##.#     ",
        "######.##.########.##.######",
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
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            maze[r][c] = when (layout[r][c]) {
                '#' -> WALL
                '.' -> PELLET
                else -> EMPTY
            }
        }
    }
    return maze
}

fun getNextPosition(pos: Pair<Int, Int>, direction: Direction): Pair<Int, Int> {
    return when (direction) {
        Direction.UP -> pos.copy(first = pos.first - 1)
        Direction.DOWN -> pos.copy(first = pos.first + 1)
        Direction.LEFT -> pos.copy(second = pos.second - 1)
        Direction.RIGHT -> pos.copy(second = pos.second + 1)
        Direction.NONE -> pos
    }
}
