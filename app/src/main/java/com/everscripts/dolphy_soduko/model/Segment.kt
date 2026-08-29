package com.everscripts.dolphy_soduko.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a single layer in a bottle.
 * In Phase 1, these are colors. In later phases, they can be Dolphy fish types.
 */
enum class Segment(val color: Color) {
    COLOR_1(Color(0xFFE57373)), // Red
    COLOR_2(Color(0xFF81C784)), // Green
    COLOR_3(Color(0xFF64B5F6)), // Blue
    COLOR_4(Color(0xFFFFF176)), // Yellow
    COLOR_5(Color(0xFFBA68C8)), // Purple
    COLOR_6(Color(0xFFFFB74D)), // Orange
    COLOR_7(Color(0xFF4DB6AC)), // Teal
    COLOR_8(Color(0xFFA1887F)), // Brown
    COLOR_9(Color(0xFF90A4AE)); // Blue Grey

    companion object {
        fun fromId(id: Int): Segment = entries[id % entries.size]
    }
}
