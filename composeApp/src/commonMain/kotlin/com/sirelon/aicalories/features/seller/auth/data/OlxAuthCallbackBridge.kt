package com.sirelon.aicalories.features.seller.auth.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object OlxAuthCallbackBridge {
    private val mutex = Mutex()
    private val callbackEvents = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val callbacks: Flow<String> = callbackEvents
        .asSharedFlow()
        .onEach { mutex.withLock { callbackEvents.resetReplayCache() } }

    fun onNewUri(url: String) {
        publishCallback(url)
    }

    fun publishCallback(url: String) {
        callbackEvents.tryEmit(url)
    }
}
