package com.everscripts.dolphy_soduko.domain.logic

import com.everscripts.dolphy_soduko.model.Bottle
import com.everscripts.dolphy_soduko.model.GameSegment
import com.everscripts.dolphy_soduko.model.Segment

object PourRuleEngine {

    /**
     * Validates if a pour from source to target is legal.
     */
    fun canPour(source: Bottle, target: Bottle): Boolean {
        if (source.id == target.id) return false
        if (source.isEmpty) return false
        if (target.isFull) return false
        
        val sourceTop = source.topSegment?.type ?: return false
        
        // Target must be empty OR top segment must match source top segment
        return target.isEmpty || target.topSegment?.type == sourceTop
    }

    /**
     * Executes a pour operation. Returns the new list of bottles.
     * Pours all matching consecutive segments of the same type.
     */
    fun pour(bottles: List<Bottle>, sourceId: Int, targetId: Int): List<Bottle> {
        val source = bottles.find { it.id == sourceId } ?: return bottles
        val target = bottles.find { it.id == targetId } ?: return bottles
        
        if (!canPour(source, target)) return bottles

        val sourceSegments = source.segments.toMutableList()
        val targetSegments = target.segments.toMutableList()
        val typeToPour = source.topSegment!!.type
        
        while (sourceSegments.isNotEmpty() && 
               sourceSegments.last().type == typeToPour && 
               targetSegments.size < target.maxCapacity) {
            val segment = sourceSegments.removeAt(sourceSegments.size - 1)
            // When moving to target, always ensure it is revealed
            targetSegments.add(segment.copy(isHidden = false))
        }
        
        return bottles.map { bottle ->
            when (bottle.id) {
                sourceId -> bottle.copy(segments = sourceSegments).revealTop()
                targetId -> bottle.copy(segments = targetSegments)
                else -> bottle
            }
        }
    }

    /**
     * Calculates how many segments will be poured.
     */
    fun calculatePourCount(source: Bottle, target: Bottle): Int {
        if (!canPour(source, target)) return 0
        val typeToPour = source.topSegment!!.type
        var count = 0
        val sourceSegments = source.segments
        val targetSize = target.segments.size
        
        for (i in sourceSegments.size - 1 downTo 0) {
            if (sourceSegments[i].type == typeToPour && (targetSize + count) < target.maxCapacity) {
                count++
            } else {
                break
            }
        }
        return count
    }

    /**
     * Checks if the game is won (all bottles are either empty or full and unified).
     */
    fun isGameWon(bottles: List<Bottle>): Boolean {
        return bottles.all { it.isEmpty || it.isSolved }
    }
}
