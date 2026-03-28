package com.sirelon.aicalories

import androidx.compose.ui.window.ComposeViewport
import com.sirelon.aicalories.features.sellerauth.data.OlxAuthCallbackBridge
import kotlinx.browser.window

fun main() {
    publishOlxCallbackIfPresent()
    ComposeViewport {
        App()
    }
}

private fun publishOlxCallbackIfPresent() {
    val href = window.location.href
    if (href.contains("code=") || href.contains("error=")) {
        OlxAuthCallbackBridge.publishCallback(href)
    }
}
