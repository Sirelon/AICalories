package com.sirelon.aicalories.supabase

import com.sirelon.aicalories.supabase.model.FoodEntryRecord
import com.sirelon.aicalories.supabase.model.FoodEntryToFileInsert
import com.sirelon.aicalories.supabase.model.StorageObjectRecord
import com.sirelon.aicalories.supabase.response.ReportAnalysisEntryResponse
import com.sirelon.aicalories.supabase.response.ReportAnalysisSummaryResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val STORAGE_BUCKET_NAME = "aicalories"
private const val ANALYZE_FUNCTION_NAME = "analize-food"

class SupabaseClient {
    @OptIn(SupabaseInternal::class)
    private val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_KEY
        ) {
            httpConfig {
                install(HttpTimeout) {
                    requestTimeoutMillis = 60_000
                    connectTimeoutMillis = 15_000
                    socketTimeoutMillis = 60_000
                }
            }
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Functions)
            install(Realtime)
        }
    }

    fun uploadFile(path: String, byteArray: ByteArray): Flow<UploadStatus> {
        return flow {
            val userId = ensureAuthenticatedUserId()

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

    private suspend fun ensureAuthenticatedUserId(): String {
        val authPlugin = client.auth
        authPlugin.awaitInitialization()

        return sessionMutex.withLock {
            retrieveUserId(authPlugin) ?: run {
                authenticateWithDefaultCredentials(authPlugin)
                retrieveUserId(authPlugin)
            }
        } ?: error("Unable to determine Supabase user id after authentication")
    }

    private suspend fun authenticateWithDefaultCredentials(authPlugin: Auth) {
        val email = SupabaseConfig.SUPABASE_DEFAULT_EMAIL
        val password = SupabaseConfig.SUPABASE_DEFAULT_PASSWORD

        runCatching {
            authPlugin.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }.recoverCatching { signInError ->
            if (signInError is RestException) {
                // In case the seed user was removed, create it on the fly.
                runCatching {
                    authPlugin.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                }.onFailure { signUpError ->
                    throw signUpError
                }

                authPlugin.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            } else {
                throw signInError
            }
        }.getOrThrow()
    }

    private suspend fun retrieveUserId(authPlugin: Auth): String? {
        return authPlugin.currentSessionOrNull()?.user?.id ?: runCatching {
            authPlugin.retrieveUserForCurrentSession().id
        }.getOrNull()
    }

    suspend fun createFoodEntry(note: String?): Long {
        val userId = ensureAuthenticatedUserId()

        val result = client
            .postgrest["food_entry"]
            .insert(
                mapOf(
                    "note" to note?.ifBlank { null },
                    "user_id" to userId,
                )
            ) {
                select()
            }

        return result.decodeSingle<FoodEntryRecord>().id
    }

    suspend fun linkFilesToFoodEntry(foodEntryId: Long, fileIds: List<String>) {
        if (fileIds.isEmpty()) return

        ensureAuthenticatedUserId()

        client
            .postgrest["food_entry_to_file"]
            .insert(
                values = fileIds.map { fileId ->
                    FoodEntryToFileInsert(
                        fileId = fileId,
                        foodEntryId = foodEntryId,
                    )
                },
            )
    }

    suspend fun fetchStorageObjectIds(paths: List<String>): Map<String, String> {
        if (paths.isEmpty()) return emptyMap()

        ensureAuthenticatedUserId()

        val result = client
            .postgrest
            .from(schema = "storage", table = "objects")
            .select {
                filter {
                    eq(column = "bucket_id", value = STORAGE_BUCKET_NAME)
                    isIn(column = "name", values = paths)
                }
            }

        return result
            .decodeList<StorageObjectRecord>()
            .associate { it.name to it.id }
    }

    suspend fun invokeFoodEntryAnalysis(foodEntryId: Long) {
        ensureAuthenticatedUserId()

        client
            .functions
            .invoke(
                function = ANALYZE_FUNCTION_NAME,
                body = buildJsonObject {
                    put("foodEntryId", foodEntryId)
                },
            )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun buildStoragePath(userId: String, originalPath: String): String {
        val sanitizedName = originalPath
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .ifBlank { originalPath }
            .trim()

        val safeName = sanitizedName
            .replace(Regex("""[^\w.\-]"""), "_")
            .takeIf { it.isNotBlank() }
            ?: "upload_${Uuid.random()}"

        return "$userId/${Uuid.random()}_$safeName"
    }

    @OptIn(SupabaseExperimental::class)
    fun observeReportSummary(foodEntryId: Long): Flow<ReportAnalysisSummaryResponse?> {
        return client
            .postgrest["report_analyse_summary"]
            .selectAsFlow(
                primaryKey = ReportAnalysisSummaryResponse::id,
                filter = FilterOperation(
                    column = "food_entry_id",
                    operator = FilterOperator.EQ,
                    value = foodEntryId,
                ),
            )
            .map { summaries ->
                summaries.firstOrNull()
            }
            .onStart {
                ensureAuthenticatedUserId()
            }
    }

    @OptIn(SupabaseExperimental::class)
    fun observeReportEntries(reportAnalyseId: Long): Flow<List<ReportAnalysisEntryResponse>> {
        return client
            .postgrest["report_analyse_entry"]
            .selectAsFlow(
                primaryKey = ReportAnalysisEntryResponse::id,
                filter = FilterOperation(
                    column = "report_analyse_id",
                    operator = FilterOperator.EQ,
                    value = reportAnalyseId,
                ),
            )
    }
}
