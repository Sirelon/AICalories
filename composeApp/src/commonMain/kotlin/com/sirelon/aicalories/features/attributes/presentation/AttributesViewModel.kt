package com.sirelon.aicalories.features.attributes.presentation

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.attributes.data.OlxAttributesRepository
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class AttributesViewModel(
    private val repository: OlxAttributesRepository,
) : BaseViewModel<AttributesContract.AttributesState, AttributesContract.AttributesEvent, Nothing>() {

    override fun initialState() = AttributesContract.AttributesState()

    private val retryTrigger = MutableStateFlow(0)

    init {
        retryTrigger
            .onEach { setState { it.copy(isLoading = true, errorMessage = null) } }
            .flatMapLatest {
                repository.getAttributes()
                    .catch { error ->
                        setState {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Unknown error",
                            )
                        }
                    }
            }
            .onEach { attributes ->
                setState { it.copy(isLoading = false, attributes = attributes) }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: AttributesContract.AttributesEvent) {
        when (event) {
            AttributesContract.AttributesEvent.Retry -> retryTrigger.update { it + 1 }
        }
    }
}
