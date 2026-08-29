package com.everscripts.dolphy_soduko.model

/**
 * Wrapper for Segment that adds hidden state for progressive difficulty.
 */
data class GameSegment(
    val type: Segment,
    val isHidden: Boolean = false
)
