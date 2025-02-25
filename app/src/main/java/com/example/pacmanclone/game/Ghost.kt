package com.example.pacmanclone.game

data class Ghost(val position: Pair<Int, Int>) {
    fun move(maze: List<List<Int>>, pacmanPos: Pair<Int, Int>): Ghost {
        val possibleDirections = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
        val validMoves = possibleDirections.map { getNextPosition(position, it) }
            .filter { pos ->
                val row = pos.first
                val col = pos.second
                row in maze.indices && col in maze[0].indices && maze[row][col] != WALL
            }
        if (validMoves.isEmpty()) return this
        val bestMove = validMoves.minByOrNull { manhattanDistance(it, pacmanPos) }!!
        return copy(position = bestMove)
    }
}

fun manhattanDistance(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
    return kotlin.math.abs(a.first - b.first) + kotlin.math.abs(a.second - b.second)
}
