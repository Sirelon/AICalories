package com.sirelon.aicalories.features.sellerauth.data

import com.sirelon.aicalories.features.sellerauth.domain.OlxAuthCallback
import com.sirelon.aicalories.platform.PlatformTargets
import io.ktor.http.Url

class DefaultOlxRedirectHandler : OlxRedirectHandler {
    override fun buildRedirectUri(platform: PlatformTargets): String = OlxConfig.redirectUri

    override fun parseCallback(url: String): OlxAuthCallback {
        val parsedUrl = Url(url)
        return OlxAuthCallback(
            code = parsedUrl.parameters["code"],
            state = parsedUrl.parameters["state"],
            error = parsedUrl.parameters["error"],
            errorDescription = parsedUrl.parameters["error_description"],
        )
    }
}
