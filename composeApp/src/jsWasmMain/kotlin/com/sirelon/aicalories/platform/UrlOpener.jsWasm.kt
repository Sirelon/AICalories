package com.sirelon.aicalories.platform

import kotlinx.browser.window

actual fun openUrl(url: String) {
    if (url.isBlank()) return
    window.open(url, "_blank")
}
