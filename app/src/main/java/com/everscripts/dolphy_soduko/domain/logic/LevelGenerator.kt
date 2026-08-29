package com.everscripts.dolphy_soduko.domain.logic

import com.everscripts.dolphy_soduko.model.Bottle
import com.everscripts.dolphy_soduko.model.GameSegment
import com.everscripts.dolphy_soduko.model.Segment
import kotlin.random.Random

class LevelGenerator(private val seed: Long) {
    private val random = Random(seed)

    /**
     * Generates a level with progressive difficulty.
     * Scales color count, scramble moves, and hidden segments.
     */
    fun generate(level: Int): List<Bottle> {
        // 1. Calculate parameters based on level
        val colorCount = when {
            level == 1 -> 4
            level <= 50 -> 5
            level <= 150 -> 6
            level <= 250 -> 7
            else -> 8
        }.coerceAtMost(9)

        val emptyBottles = if (level < 200) 2 else 1
        val filledBottlesCount = 10 - emptyBottles
        
        // Ensure we don't have more colors than filled bottles
        val actualColorCount = colorCount.coerceAtMost(filledBottlesCount)

        // 2. Initialize solved state
        val bottles = mutableListOf<Bottle>()
        for (i in 0 until actualColorCount) {
            val segments = List(4) { GameSegment(Segment.fromId(i), isHidden = false) }
            bottles.add(Bottle(id = i, segments = segments))
        }
        
        // Add extra filled bottles with existing colors if needed
        for (i in actualColorCount until filledBottlesCount) {
            val segments = List(4) { GameSegment(Segment.fromId(i % actualColorCount), isHidden = false) }
            bottles.add(Bottle(id = i, segments = segments))
        }

        // Add empty bottles
        for (i in filledBottlesCount until 10) {
            bottles.add(Bottle(id = i, segments = emptyList()))
        }

        // 3. Scramble using random walk (Reverse moves)
        val scrambleMoves = (level * 2 + 10).coerceAtMost(200)
        repeat(scrambleMoves) {
            val nonEmpty = bottles.filter { !it.isEmpty }
            val nonFull = bottles.filter { !it.isFull }
            
            if (nonEmpty.isNotEmpty() && nonFull.isNotEmpty()) {
                val src = nonEmpty.random(random)
                val dst = nonFull.filter { it.id != src.id }.randomOrNull(random)
                
                if (dst != null) {
                    val segment = src.segments.last()
                    val newSrcSegments = src.segments.dropLast(1)
                    val newDstSegments = dst.segments + segment
                    
                    val srcIndex = bottles.indexOfFirst { it.id == src.id }
                    val dstIndex = bottles.indexOfFirst { it.id == dst.id }
                    
                    bottles[srcIndex] = src.copy(segments = newSrcSegments)
                    bottles[dstIndex] = dst.copy(segments = newDstSegments)
                }
            }
        }

        // 4. Apply Mystery Fish (Hide lower segments)
        // Complexity of hiding increases with level
        val hideProbability = if (level == 1) 0f else (level / 300f).coerceIn(0.2f, 0.7f)
        
        return bottles.map { bottle ->
            if (bottle.isEmpty) return@map bottle
            
            val newSegments = bottle.segments.mapIndexed { index, segment ->
                // Never hide the top segment
                if (index == bottle.segments.size - 1) {
                    segment.copy(isHidden = false)
                } else {
                    // Randomly hide based on level difficulty
                    val shouldHide = random.nextFloat() < hideProbability
                    segment.copy(isHidden = shouldHide)
                }
            }
            bottle.copy(segments = newSegments)
        }
    }

    /**
     * Generates a fixed high-difficulty level for the Daily Challenge.
     */
    fun generateHardLevel(): List<Bottle> {
        val colorCount = 9
        val emptyBottles = 1
        val filledBottlesCount = 10 - emptyBottles
        val actualColorCount = 9

        // 1. Initialize solved state
        val bottles = mutableListOf<Bottle>()
        for (i in 0 until actualColorCount) {
            val segments = List(4) { GameSegment(Segment.fromId(i), isHidden = false) }
            bottles.add(Bottle(id = i, segments = segments))
        }
        
        // Final bottle
        bottles.add(Bottle(id = 9, segments = emptyList()))

        // 2. Scramble heavily (200 moves)
        repeat(250) {
            val nonEmpty = bottles.filter { !it.isEmpty }
            val nonFull = bottles.filter { !it.isFull }
            
            if (nonEmpty.isNotEmpty() && nonFull.isNotEmpty()) {
                val src = nonEmpty.random(random)
                val dst = nonFull.filter { it.id != src.id }.randomOrNull(random)
                
                if (dst != null) {
                    val segment = src.segments.last()
                    val newSrcSegments = src.segments.dropLast(1)
                    val newDstSegments = dst.segments + segment
                    
                    val srcIndex = bottles.indexOfFirst { it.id == src.id }
                    val dstIndex = bottles.indexOfFirst { it.id == dst.id }
                    
                    bottles[srcIndex] = src.copy(segments = newSrcSegments)
                    bottles[dstIndex] = dst.copy(segments = newDstSegments)
                }
            }
        }

        // 3. Max Mystery (70% probability)
        return bottles.map { bottle ->
            if (bottle.isEmpty) return@map bottle
            val newSegments = bottle.segments.mapIndexed { index, segment ->
                if (index == bottle.segments.size - 1) {
                    segment.copy(isHidden = false)
                } else {
                    segment.copy(isHidden = random.nextFloat() < 0.7f)
                }
            }
            bottle.copy(segments = newSegments)
        }
    }
}
