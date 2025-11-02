package com.sirelon.aicalories.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.currentSessionOrNull
import io.github.jan.supabase.auth.retrieveUserForCurrentSession
import io.github.jan.supabase.auth.signInAnonymously
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val STORAGE_BUCKET_NAME = "aicalories"

class SupabaseClient {
    private val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    fun uploadFile(path: String, byteArray: ByteArray): Flow<UploadStatus> {
        return flow {
            val userId = ensureAnonymousUserId()
            val storagePath = buildStoragePath(userId, path)

            emitAll(
                client
                    .storage
                    .from(STORAGE_BUCKET_NAME)
                    .uploadAsFlow(path = storagePath, data = byteArray)
            )
        }
    }

    private val sessionMutex = Mutex()

    private suspend fun ensureAnonymousUserId(): String? {
        val authPlugin = client.auth
        authPlugin.awaitInitialization()

        return sessionMutex.withLock {
            val currentSession = authPlugin.currentSessionOrNull()
            when {
                currentSession?.user != null -> currentSession.user?.id
                currentSession == null -> {
                    authPlugin.signInAnonymously()
                    authPlugin.currentSessionOrNull()?.user?.id ?: runCatching {
                        authPlugin.retrieveUserForCurrentSession()
                    }.getOrNull()?.id
                }

                else -> runCatching {
                    authPlugin.retrieveUserForCurrentSession()
                }.getOrNull()?.id
            }
        }
    }

    private fun buildStoragePath(userId: String?, originalPath: String): String {
        val sanitizedName = originalPath
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .ifBlank { originalPath }

        return if (!userId.isNullOrBlank()) {
            "$userId/$sanitizedName"
        } else {
            sanitizedName
        }
    }
}
