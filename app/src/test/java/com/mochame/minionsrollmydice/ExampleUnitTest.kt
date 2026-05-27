package com.mochame.minionsrollmydice

import com.mochame.minionsrollmydice.domain.AttributeType
import com.mochame.minionsrollmydice.domain.CharacterAttributes
import com.mochame.minionsrollmydice.domain.DiceRollerStateMachine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun test_default_attributes_are_zero() {
        val attr = CharacterAttributes()
        assertEquals(0, attr.wisdom)
        assertEquals(0, attr.insight)
        assertEquals(0, attr.inspired)
        assertEquals(0, attr.totalAllocated())
        assertEquals(5, attr.remainingPoints())
    }

    @Test
    fun test_can_increment_and_decrement() {
        var attr = CharacterAttributes()
        
        // Increment wisdom
        assertTrue(attr.canIncrement(AttributeType.WISDOM))
        attr = attr.increment(AttributeType.WISDOM)
        assertEquals(1, attr.wisdom)
        assertEquals(4, attr.remainingPoints())

        // Decrement wisdom
        assertTrue(attr.canDecrement(AttributeType.WISDOM))
        attr = attr.decrement(AttributeType.WISDOM)
        assertEquals(0, attr.wisdom)
        assertEquals(5, attr.remainingPoints())
    }

    @Test
    fun test_point_budget_limit() {
        var attr = CharacterAttributes()
        
        // Allocate all 5 points
        attr = attr.increment(AttributeType.WISDOM)  // 1
        attr = attr.increment(AttributeType.WISDOM)  // 2
        attr = attr.increment(AttributeType.WISDOM)  // 3
        attr = attr.increment(AttributeType.INSIGHT) // 4
        attr = attr.increment(AttributeType.INSPIRED)// 5

        assertEquals(3, attr.wisdom)
        assertEquals(1, attr.insight)
        assertEquals(1, attr.inspired)
        assertEquals(5, attr.totalAllocated())
        assertEquals(0, attr.remainingPoints())

        // Ensure we cannot increment further
        assertFalse(attr.canIncrement(AttributeType.WISDOM))
        val noChange = attr.increment(AttributeType.WISDOM)
        assertEquals(3, noChange.wisdom) // still 3
    }

    @Test
    fun test_modifier_calculation() {
        val attr = CharacterAttributes(wisdom = 2, insight = 2, inspired = 1)
        assertEquals(5, attr.calculateTotalModifier())
    }

    @Test
    fun test_state_machine_tumble() {
        val sm = DiceRollerStateMachine()
        assertTrue(sm.getState() is DiceRollerStateMachine.State.Idle)

        val attr = CharacterAttributes(wisdom = 1, insight = 1, inspired = 1) // +3 modifier
        
        // Start Roll
        val rollingState = sm.startRoll(20, attr) as DiceRollerStateMachine.State.Rolling
        assertEquals(3, rollingState.modifier)
        assertEquals(20, rollingState.diceSides)

        // Tick
        val tickedState = sm.tickAnimation(12) as DiceRollerStateMachine.State.Rolling
        assertEquals(12, tickedState.tempValue)

        // Complete Roll
        val rolledState = sm.completeRoll(15, attr) as DiceRollerStateMachine.State.Rolled
        assertEquals(15, rolledState.baseRoll)
        assertEquals(3, rolledState.modifier)
        assertEquals(18, rolledState.finalTotal)

        // Verify history
        assertEquals(1, rolledState.history.size)
        assertEquals(18, rolledState.history[0].total)
    }
}