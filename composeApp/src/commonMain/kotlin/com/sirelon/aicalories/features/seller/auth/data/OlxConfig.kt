package com.sirelon.aicalories.features.seller.auth.data

import com.sirelon.aicalories.config.AppConfig

object OlxConfig {
    const val apiVersion = "2.0"
    const val authTokenPath = "/open/oauth/token"
    const val partnerApiBasePath = "/partner"
    const val defaultRefreshSafetyWindowSeconds = 60L

    val clientId: String
        get() = AppConfig.olxClientId

    val clientSecret: String
        get() = AppConfig.olxClientSecret

    val scope: String
        get() = AppConfig.olxScope

    val authBaseUrl: String
        get() = AppConfig.olxAuthBaseUrl

    val apiBaseUrl: String
        get() = AppConfig.olxApiBaseUrl

    val redirectUri: String
        get() = AppConfig.olxRedirectUri
}
