package com.sirelon.aicalories.features.common.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State, Event, Effect> : ViewModel() {

    private val _state = MutableStateFlow<State>(initialState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>()
    val effects = _effects.asSharedFlow()

    abstract fun initialState(): State

    abstract fun onEvent(event: Event)

    fun setState(function: (State) -> State) {
        _state.update(function)
    }

    fun postEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }
}
