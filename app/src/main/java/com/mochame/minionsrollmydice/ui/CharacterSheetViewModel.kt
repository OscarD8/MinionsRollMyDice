package com.mochame.minionsrollmydice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mochame.minionsrollmydice.data.CharacterRepository
import com.mochame.minionsrollmydice.domain.AttributeType
import com.mochame.minionsrollmydice.domain.CharacterAttributes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * UDF State container representing the Character Sheet UI.
 */
data class CharacterSheetUiState(
    val attributes: CharacterAttributes = CharacterAttributes(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@KoinViewModel
class CharacterSheetViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val logger = Logger.withTag("CharacterSheetForensics")
    private val _uiState = MutableStateFlow(CharacterSheetUiState())
    val uiState: StateFlow<CharacterSheetUiState> = _uiState.asStateFlow()

    init {
        // Collect attributes from local persistence to populate the sheet initially
        viewModelScope.launch {
            repository.observeAttributes().collect { savedAttributes ->
                _uiState.update { it.copy(attributes = savedAttributes) }
            }
        }
    }

    fun incrementAttribute(type: AttributeType) {
        _uiState.update { currentState ->
            if (!currentState.attributes.canIncrement(type)) {
                logger.w { "POINT_LIMIT_EXCEEDED: Cannot increment $type. Total: ${currentState.attributes.totalAllocated()}/10" }
                return@update currentState
            }
            val updated = currentState.attributes.increment(type)
            logger.i { "ATTRIBUTE_CHANGED: Incremented $type to ${updated.totalAllocated()}" }
            currentState.copy(attributes = updated, saveSuccess = false)
        }
    }

    fun decrementAttribute(type: AttributeType) {
        _uiState.update { currentState ->
            if (!currentState.attributes.canDecrement(type)) {
                logger.w { "POINT_MIN_LIMIT_EXCEEDED: Cannot decrement $type" }
                return@update currentState
            }
            val updated = currentState.attributes.decrement(type)
            logger.i { "ATTRIBUTE_CHANGED: Decremented $type to ${updated.totalAllocated()}" }
            currentState.copy(attributes = updated, saveSuccess = false)
        }
    }

    fun saveAttributes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                logger.i { "REGISTRY_SAVE_START: Attempting to save character configuration." }
                repository.saveAttributes(_uiState.value.attributes)
                logger.i { "REGISTRY_SAVE_SUCCESS: Configuration persisted safely." }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                logger.e(e) { "REGISTRY_SAVE_FAILED: Database persistence failure." }
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}") }
            }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
