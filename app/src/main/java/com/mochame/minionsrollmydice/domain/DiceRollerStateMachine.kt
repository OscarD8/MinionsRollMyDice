package com.mochame.minionsrollmydice.domain

/**
 * Pure Kotlin state machine managing the decoupled dice rolling logic.
 * Keeps UI components isolated from logic states.
 */
class DiceRollerStateMachine {

    sealed interface State {
        val history: List<RollResult>

        data class Idle(
            override val history: List<RollResult> = emptyList()
        ) : State

        data class Rolling(
            val tempValue: Int,
            val diceSides: Int,
            val modifier: Int,
            override val history: List<RollResult>
        ) : State

        data class Rolled(
            val baseRoll: Int,
            val modifier: Int,
            val finalTotal: Int,
            override val history: List<RollResult>
        ) : State
    }

    private var currentState: State = State.Idle()

    fun getState(): State = currentState

    fun startRoll(diceSides: Int, attributes: CharacterAttributes): State {
        val modifier = attributes.calculateTotalModifier()
        currentState = State.Rolling(
            tempValue = (1..diceSides).random(),
            diceSides = diceSides,
            modifier = modifier,
            history = currentState.history
        )
        return currentState
    }

    fun tickAnimation(tempValue: Int): State {
        val state = currentState
        if (state is State.Rolling) {
            currentState = state.copy(tempValue = tempValue)
        }
        return currentState
    }

    fun completeRoll(finalBaseRoll: Int, attributes: CharacterAttributes): State {
        val state = currentState
        if (state is State.Rolling) {
            val modifier = attributes.calculateTotalModifier()
            val total = finalBaseRoll + modifier
            val result = RollResult(
                id = System.currentTimeMillis().toString(),
                baseRoll = finalBaseRoll,
                modifier = modifier,
                total = total,
                timestamp = System.currentTimeMillis()
            )
            val updatedHistory = listOf(result) + state.history
            currentState = State.Rolled(
                baseRoll = finalBaseRoll,
                modifier = modifier,
                finalTotal = total,
                history = updatedHistory
            )
        }
        return currentState
    }

    fun clearHistory(): State {
        currentState = State.Idle(history = emptyList())
        return currentState
    }
}

data class RollResult(
    val id: String,
    val baseRoll: Int,
    val modifier: Int,
    val total: Int,
    val timestamp: Long
)
