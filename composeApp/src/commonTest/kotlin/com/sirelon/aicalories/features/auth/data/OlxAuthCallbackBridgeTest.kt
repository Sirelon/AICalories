package com.sirelon.aicalories.features.auth.data

import com.sirelon.aicalories.features.seller.auth.data.OlxAuthCallbackBridge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class OlxAuthCallbackBridgeTest {

    @Test
    fun `callback published before collection is delivered once`() = runBlocking {
        OlxAuthCallbackBridge.publishCallback("selolxai://olx-auth/callback?code=one")

        val callback = withTimeout(1_000) {
            OlxAuthCallbackBridge.callbacks.first()
        }

        assertEquals("selolxai://olx-auth/callback?code=one", callback)
    }
}
