package com.everscripts.dolphy_soduko.domain.logic

import com.everscripts.dolphy_soduko.model.Bottle
import com.everscripts.dolphy_soduko.model.Segment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.*

object GameSolver {

    data class Move(val fromId: Int, val toId: Int)

    /**
     * Finds the next best move using a priority-based search.
     * Heuristic: Minimize the total number of "color breaks" (adjacent different colors) in bottles.
     */
    suspend fun solve(initialBottles: List<Bottle>): List<Move>? {
        val queue = PriorityQueue<Node>(compareBy { it.cost + it.heuristic })
        val visited = mutableSetOf<List<List<Segment>>>()

        val initialNormalized = initialBottles.map { b -> b.segments.map { it.type } }
        queue.add(Node(initialBottles, emptyList(), 0, calculateHeuristic(initialBottles)))
        visited.add(initialNormalized)

        var nodesVisited = 0
        while (queue.isNotEmpty()) {
            // Cooperative cancellation: Stop immediately if the job was cancelled
            currentCoroutineContext().ensureActive()

            val current = queue.poll()!!
            nodesVisited++

            if (PourRuleEngine.isGameWon(current.bottles)) {
                return current.path
            }

            for (src in current.bottles) {
                if (src.isEmpty) continue
                for (dst in current.bottles) {
                    if (src.id == dst.id) continue
                    if (PourRuleEngine.canPour(src, dst)) {
                        val nextBottles = PourRuleEngine.pour(current.bottles, src.id, dst.id)
                        val normalized = nextBottles.map { b -> b.segments.map { it.type } }

                        if (!visited.contains(normalized)) {
                            visited.add(normalized)
                            val nextPath = current.path + Move(src.id, dst.id)
                            queue.add(Node(nextBottles, nextPath, current.cost + 1, calculateHeuristic(nextBottles)))
                        }
                    }
                }
            }

            // Limit search space for performance on mobile
            if (nodesVisited > 8000) break
        }
        return null
    }

    private fun calculateHeuristic(bottles: List<Bottle>): Int {
        var breaks = 0
        for (bottle in bottles) {
            if (bottle.isEmpty) continue
            val segments = bottle.segments
            for (i in 0 until segments.size - 1) {
                if (segments[i].type != segments[i+1].type) {
                    breaks++
                }
            }
            // Penalty for not being full (to encourage complete bottles)
            if (segments.size > 0 && segments.size < bottle.maxCapacity) {
                breaks++
            }
        }
        return breaks
    }

    private data class Node(
        val bottles: List<Bottle>,
        val path: List<Move>,
        val cost: Int,
        val heuristic: Int
    )
}
