package com.sirelon.aicalories.features.seller.auth.data

import com.sirelon.aicalories.features.seller.auth.domain.OlxAuthCallback
import com.sirelon.aicalories.features.seller.auth.domain.OlxLaunchResult
import com.sirelon.aicalories.features.seller.auth.domain.OlxPendingAuthSession
import com.sirelon.aicalories.features.seller.auth.domain.OlxTokens
import com.sirelon.aicalories.platform.PlatformTargets
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface OlxCredentialsProvider {
    suspend fun getClientId(): String

    suspend fun getClientSecret(): String
}

interface OlxTokenStore {
    suspend fun read(): OlxTokens?

    suspend fun write(tokens: OlxTokens)

    suspend fun clear()
}

interface OlxAuthSessionStore {
    suspend fun read(): OlxPendingAuthSession?

    suspend fun write(session: OlxPendingAuthSession)

    suspend fun clear()
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

class InMemoryOlxTokenStore : OlxTokenStore {
    private val mutex = Mutex()
    private var tokens: OlxTokens? = null

    override suspend fun read(): OlxTokens? = mutex.withLock { tokens }

    override suspend fun write(tokens: OlxTokens) {
        mutex.withLock {
            this.tokens = tokens
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            tokens = null
        }
    }
}

class InMemoryOlxAuthSessionStore : OlxAuthSessionStore {
    private val mutex = Mutex()
    private var session: OlxPendingAuthSession? = null

    override suspend fun read(): OlxPendingAuthSession? = mutex.withLock { session }

    override suspend fun write(session: OlxPendingAuthSession) {
        mutex.withLock {
            this.session = session
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            session = null
        }
    }
}
