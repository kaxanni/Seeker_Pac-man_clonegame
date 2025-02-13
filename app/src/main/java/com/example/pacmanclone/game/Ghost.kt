// Ghost.kt
package com.example.pacmanclone.game

data class Ghost(val position: Pair<Int, Int>) {
    /**
     * Moves the ghost toward pacman by choosing the valid move that minimizes
     * the Manhattan distance between the ghost’s new position and pacmanPos.
     */
    fun move(maze: List<List<Int>>, pacmanPos: Pair<Int, Int>): Ghost {
        // List of possible directions.
        val possibleDirections = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
        // Calculate next positions for all possible directions and filter out invalid moves (i.e. hitting a wall).
        val validMoves = possibleDirections.map { getNextPosition(position, it) }
            .filter { pos ->
                val row = pos.first
                val col = pos.second
                row in maze.indices && col in maze[0].indices && maze[row][col] != WALL
            }
        // If there are no valid moves, remain in the same position.
        if (validMoves.isEmpty()) return this
        // Choose the move that minimizes the Manhattan distance to Pac-Man.
        val bestMove = validMoves.minByOrNull { manhattanDistance(it, pacmanPos) }!!
        return copy(position = bestMove)
    }
}

/**
 * Returns the Manhattan distance between two grid positions.
 */
fun manhattanDistance(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
    return kotlin.math.abs(a.first - b.first) + kotlin.math.abs(a.second - b.second)
}
