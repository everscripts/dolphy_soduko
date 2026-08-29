package com.everscripts.dolphy_soduko

import com.everscripts.dolphy_soduko.domain.logic.GameSolver
import com.everscripts.dolphy_soduko.domain.logic.PourRuleEngine
import com.everscripts.dolphy_soduko.model.Bottle
import com.everscripts.dolphy_soduko.model.GameSegment
import com.everscripts.dolphy_soduko.model.Segment
import org.junit.Assert.*
import org.junit.Test

class GameLogicTest {

    @Test
    fun testCanPourMatchingColors() {
        val bottle1 = Bottle(id = 1, segments = listOf(GameSegment(Segment.COLOR_1)))
        val bottle2 = Bottle(id = 2, segments = listOf(GameSegment(Segment.COLOR_1)))
        
        assertTrue("Should be able to pour same colors", PourRuleEngine.canPour(bottle1, bottle2))
    }

    @Test
    fun testCanPourIntoEmpty() {
        val bottle1 = Bottle(id = 1, segments = listOf(GameSegment(Segment.COLOR_1)))
        val bottle2 = Bottle(id = 2, segments = emptyList())
        
        assertTrue("Should be able to pour into empty bottle", PourRuleEngine.canPour(bottle1, bottle2))
    }

    @Test
    fun testCannotPourMismatchedColors() {
        val bottle1 = Bottle(id = 1, segments = listOf(GameSegment(Segment.COLOR_1)))
        val bottle2 = Bottle(id = 2, segments = listOf(GameSegment(Segment.COLOR_2)))
        
        assertFalse("Should not be able to pour mismatched colors", PourRuleEngine.canPour(bottle1, bottle2))
    }

    @Test
    fun testCannotPourIntoFull() {
        val bottle1 = Bottle(id = 1, segments = listOf(GameSegment(Segment.COLOR_1)))
        val bottle2 = Bottle(id = 2, segments = List(4) { GameSegment(Segment.COLOR_1) })
        
        assertFalse("Should not be able to pour into full bottle", PourRuleEngine.canPour(bottle1, bottle2))
    }

    @Test
    fun testSolveSimplePuzzle() {
        // Simple case: 4 segments of Color 1 and 4 segments of Color 2
        // Scrambled into 2 bottles, with 2 empty bottles as workspace
        val b1 = Bottle(id = 0, segments = listOf(
            GameSegment(Segment.COLOR_1), GameSegment(Segment.COLOR_2),
            GameSegment(Segment.COLOR_1), GameSegment(Segment.COLOR_2)
        ))
        val b2 = Bottle(id = 1, segments = listOf(
            GameSegment(Segment.COLOR_2), GameSegment(Segment.COLOR_1),
            GameSegment(Segment.COLOR_2), GameSegment(Segment.COLOR_1)
        ))
        val b3 = Bottle(id = 2, segments = emptyList())
        val b4 = Bottle(id = 3, segments = emptyList())
        
        val initialBottles = listOf(b1, b2, b3, b4)
        val solution = GameSolver.solve(initialBottles)
        
        assertNotNull("Should find a solution for simple level", solution)
        assertTrue("Solution should have moves", solution!!.isNotEmpty())
    }
}
