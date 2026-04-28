package com.sirelon.aicalories.features.seller.ad

import kotlin.time.Clock

class AdFlowTimerStore {

    private var flowStartMs: Long = 0L
    private var generationElapsedMs: Long = 0L

    fun markGenerationStarted(nowMs: Long = Clock.System.now().toEpochMilliseconds()) {
        flowStartMs = nowMs
        generationElapsedMs = 0L
    }

    fun markGenerationCompleted(nowMs: Long = Clock.System.now().toEpochMilliseconds()): Long {
        if (flowStartMs == 0L) {
            flowStartMs = nowMs
        }
        generationElapsedMs = (nowMs - flowStartMs).coerceAtLeast(0L)
        return generationElapsedMs
    }

    fun generationElapsedMs(): Long = generationElapsedMs

    fun totalElapsedMs(nowMs: Long = Clock.System.now().toEpochMilliseconds()): Long {
        if (flowStartMs == 0L) return 0L
        return (nowMs - flowStartMs).coerceAtLeast(0L)
    }

    fun clear() {
        flowStartMs = 0L
        generationElapsedMs = 0L
    }
}
