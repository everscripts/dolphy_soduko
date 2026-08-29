package com.everscripts.dolphy_soduko.model

/**
 * Represents a bottle in the game.
 * MAX_CAPACITY is fixed at 4 segments.
 */
data class Bottle(
    val id: Int,
    val segments: List<GameSegment> = emptyList()
) {
    val maxCapacity: Int = 4
    
    val isFull: Boolean get() = segments.size >= maxCapacity
    val isEmpty: Boolean get() = segments.isEmpty()
    val topSegment: GameSegment? get() = segments.lastOrNull()
    
    val isSolved: Boolean get() = segments.size == maxCapacity && 
            segments.all { !it.isHidden && it.type == segments[0].type }

    /**
     * Checks if all segments in the bottle are of the same color.
     */
    fun isUnified(): Boolean = segments.isNotEmpty() && segments.distinctBy { it.type }.size == 1

    fun canAccept(sourceType: Segment): Boolean {
        if (isFull) return false
        if (isEmpty) return true
        return topSegment?.type == sourceType
    }

    /**
     * Ensures the top segment is always revealed.
     */
    fun revealTop(): Bottle {
        if (isEmpty) return this
        val lastIndex = segments.size - 1
        if (!segments[lastIndex].isHidden) return this
        
        val newSegments = segments.toMutableList()
        newSegments[lastIndex] = newSegments[lastIndex].copy(isHidden = false)
        return copy(segments = newSegments)
    }
}
