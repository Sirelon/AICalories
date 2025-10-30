package com.sirelon.aicalories.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.flow.Flow

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
        return client
            .storage
            .from(STORAGE_BUCKET_NAME)
            .uploadAsFlow(path = path, data = byteArray)
    }
}