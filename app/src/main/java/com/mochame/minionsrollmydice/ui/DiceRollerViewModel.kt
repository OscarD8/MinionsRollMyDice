package com.mochame.minionsrollmydice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochame.minionsrollmydice.data.CharacterRepository
import com.mochame.minionsrollmydice.domain.CharacterAttributes
import com.mochame.minionsrollmydice.domain.DiceRollerStateMachine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * UDF State container representing the Dice Roller UI and animation.
 */
data class DiceRollerUiState(
    val diceState: DiceRollerStateMachine.State = DiceRollerStateMachine.State.Idle(),
    val attributes: CharacterAttributes = CharacterAttributes()
)

@KoinViewModel
class DiceRollerViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val stateMachine = DiceRollerStateMachine()
    private val _uiState = MutableStateFlow(DiceRollerUiState(diceState = stateMachine.getState()))
    val uiState: StateFlow<DiceRollerUiState> = _uiState.asStateFlow()

    init {
        // Automatically listen to character attributes changes to dynamically compute roll modifiers
        viewModelScope.launch {
            repository.observeAttributes().collect { updatedAttributes ->
                _uiState.update { it.copy(attributes = updatedAttributes) }
            }
        }
    }

    /**
     * Executes the rolling animation safely bound to the viewModelScope.
     * Screens rotation will not leak this coroutine because the ViewModel survives configuration changes.
     */
    fun rollDice(sides: Int) {
        // Enforce the concurrency rule: bound to viewModelScope and does not run detached
        viewModelScope.launch {
            val attributes = _uiState.value.attributes
            
            // Step 1: Start Roll
            val startState = stateMachine.startRoll(sides, attributes)
            _uiState.update { it.copy(diceState = startState) }

            // Step 2: Animate with ticks
            val tickCount = 10
            val tickDelayMs = 60L
            for (i in 1..tickCount) {
                delay(tickDelayMs)
                val tempValue = (1..sides).random()
                val updatedState = stateMachine.tickAnimation(tempValue)
                _uiState.update { it.copy(diceState = updatedState) }
            }

            // Step 3: Complete Roll
            val finalResult = (1..sides).random()
            val completedState = stateMachine.completeRoll(finalResult, attributes)
            _uiState.update { it.copy(diceState = completedState) }
        }
    }

    fun clearHistory() {
        val cleared = stateMachine.clearHistory()
        _uiState.update { it.copy(diceState = cleared) }
    }
}
