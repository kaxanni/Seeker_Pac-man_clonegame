package com.example.pacmanclone.game

import java.util.PriorityQueue

data class Ghost(val position: Pair<Int, Int>) {
    fun move(maze: List<List<Int>>, pacmanPos: Pair<Int, Int>): Ghost {
        // Use A* to find the shortest path from the ghost's current position to Pac-Man.
        val path = findPath(maze, position, pacmanPos)
        // If a valid path is found (path[0] is the ghost's current position),
        // move to the next cell along the path.
        return if (path.size >= 2) copy(position = path[1]) else this
    }
}

fun manhattanDistance(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
    return kotlin.math.abs(a.first - b.first) + kotlin.math.abs(a.second - b.second)
}

fun findPath(maze: List<List<Int>>, start: Pair<Int, Int>, goal: Pair<Int, Int>): List<Pair<Int, Int>> {
    data class Node(val pos: Pair<Int, Int>, val g: Int, val f: Int)
    val openSet = PriorityQueue<Node>(compareBy { it.f })
    openSet.add(Node(start, 0, manhattanDistance(start, goal)))
    val cameFrom = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
    val gScore = mutableMapOf<Pair<Int, Int>, Int>()
    gScore[start] = 0

    val directions = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
    while (openSet.isNotEmpty()) {
        val current = openSet.poll()
        if (current!!.pos == goal) {
            // Reconstruct the path from start to goal.
            val path = mutableListOf<Pair<Int, Int>>()
            var cur = current.pos
            path.add(cur)
            while (cameFrom.containsKey(cur)) {
                cur = cameFrom[cur]!!
                path.add(cur)
            }
            path.reverse()
            return path
        }
        for (dir in directions) {
            val neighbor = getNextPosition(current.pos, dir)
            val (nr, nc) = neighbor
            if (nr !in maze.indices || nc !in maze[0].indices) continue
            if (maze[nr][nc] == WALL) continue
            val tentativeG = current.g + 1
            if (tentativeG < (gScore[neighbor] ?: Int.MAX_VALUE)) {
                cameFrom[neighbor] = current.pos
                gScore[neighbor] = tentativeG
                val fScore = tentativeG + manhattanDistance(neighbor, goal)
                openSet.add(Node(neighbor, tentativeG, fScore))
            }
        }
    }
    // If no path found, return an empty list.
    return emptyList()
}