package com.sirelon.aicalories.features.common.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlin.LazyThreadSafetyMode

abstract class BaseViewModel<State, Event, Effect> : ViewModel() {

    private val _state: MutableStateFlow<State> by lazy(LazyThreadSafetyMode.NONE) {
        MutableStateFlow(initialState())
    }
    val state: StateFlow<State>
        get() = _state

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    abstract fun initialState(): State

    abstract fun onEvent(event: Event)

    fun setState(function: (State) -> State) {
        _state.update(function)
    }

    fun postEffect(effect: Effect) {
        _effects.trySend(effect)
    }
}
