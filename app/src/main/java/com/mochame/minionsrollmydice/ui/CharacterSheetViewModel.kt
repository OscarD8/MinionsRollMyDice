package com.mochame.minionsrollmydice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            val updated = currentState.attributes.increment(type)
            currentState.copy(attributes = updated, saveSuccess = false)
        }
    }

    fun decrementAttribute(type: AttributeType) {
        _uiState.update { currentState ->
            val updated = currentState.attributes.decrement(type)
            currentState.copy(attributes = updated, saveSuccess = false)
        }
    }

    fun saveAttributes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                repository.saveAttributes(_uiState.value.attributes)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}") }
            }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
