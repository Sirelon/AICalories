package com.sirelon.aicalories.features.seller.auth.data

import com.sirelon.aicalories.features.seller.auth.domain.OlxAuthCallback
import com.sirelon.aicalories.features.seller.auth.domain.OlxLaunchResult
import com.sirelon.aicalories.features.seller.auth.domain.OlxPendingAuthSession
import com.sirelon.aicalories.features.seller.auth.domain.OlxTokens
import com.sirelon.aicalories.datastore.KeyValueStore
import com.sirelon.aicalories.datastore.createKeyValueStore
import com.sirelon.aicalories.platform.PlatformTargets
import kotlinx.serialization.json.Json

interface OlxCredentialsProvider {
    suspend fun getClientId(): String

    suspend fun getClientSecret(): String
}

class OlxTokenStore internal constructor(private val storage: KeyValueStore) {
    constructor() : this(createKeyValueStore("olx_tokens"))

    suspend fun read(): OlxTokens? =
        storage.getString(KEY)?.let { json.decodeFromString<OlxTokens>(it) }

    suspend fun write(tokens: OlxTokens) =
        storage.putString(KEY, json.encodeToString<OlxTokens>(tokens))

    suspend fun clear() = storage.remove(KEY)

    private companion object {
        const val KEY = "tokens"
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }
}

class OlxAuthSessionStore internal constructor(private val storage: KeyValueStore) {
    constructor() : this(createKeyValueStore("olx_auth_session"))

    suspend fun read(): OlxPendingAuthSession? =
        storage.getString(KEY)?.let { json.decodeFromString<OlxPendingAuthSession>(it) }

    suspend fun write(session: OlxPendingAuthSession) =
        storage.putString(KEY, json.encodeToString<OlxPendingAuthSession>(session))

    suspend fun clear() = storage.remove(KEY)

    private companion object {
        const val KEY = "session"
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }
}

interface OlxRedirectHandler {
    fun buildRedirectUri(platform: PlatformTargets = PlatformTargets): String

    fun parseCallback(url: String): OlxAuthCallback
}

interface OlxExternalAuthLauncher {
    suspend fun launch(url: String): OlxLaunchResult
}

class BuildConfigOlxCredentialsProvider : OlxCredentialsProvider {
    override suspend fun getClientId(): String = OlxConfig.clientId

    override suspend fun getClientSecret(): String = OlxConfig.clientSecret
}
