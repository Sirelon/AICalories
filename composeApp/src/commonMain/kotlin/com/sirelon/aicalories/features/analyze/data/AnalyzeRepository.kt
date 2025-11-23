package com.sirelon.aicalories.features.analyze.data

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.readByteArray
import com.sirelon.aicalories.supabase.SupabaseClient
import com.sirelon.aicalories.supabase.model.AnalyseReportData
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

class AnalyzeRepository(
    private val client: SupabaseClient,
) {

    data class UploadedFile(
        val id: String?,
        val path: String,
    )

    fun uploadFile(platformContext: PlatformContext, file: KmpFile): Flow<UploadStatus> {
        return flow {
            val flow = client.uploadFile(
                path = file.getName(platformContext)
                    ?: file.getPath(platformContext)
                    ?: Uuid.random().toString(),
                byteArray = file.readByteArray(platformContext)
            )
            emitAll(flow)
        }
    }

    fun observeAnalysis(foodEntryId: Long): Flow<AnalyseReportData> {
        return client.observeReportSummary(foodEntryId)
            .filterNotNull()
            .flatMapLatest { summary ->
                client
                    .observeReportEntries(summary.id)
                    .map { entries ->
                        AnalyseReportData(
                            summary = summary,
                            entries = entries,
                        )
                    }
            }
    }

    suspend fun createFoodEntry(note: String?, files: List<UploadedFile>): Result<Long> =
        runCatching {
            val sanitizedNote = note?.ifBlank { null }
            val foodEntryId = client.createFoodEntry(sanitizedNote)

            val missingPaths = files.mapNotNull { file ->
                file.takeIf { file.id.isNullOrBlank() }?.path
            }

            val resolvedIds = if (missingPaths.isNotEmpty()) {
                client.fetchStorageObjectIds(missingPaths)
            } else {
                emptyMap()
            }

            val fileIds = files.mapNotNull { file ->
                file.id ?: resolvedIds[file.path]
            }

            if (fileIds.isNotEmpty()) {
                client.linkFilesToFoodEntry(foodEntryId = foodEntryId, fileIds = fileIds)
            }

            foodEntryId
        }

    suspend fun requestAnalysis(foodEntryId: Long): Result<Unit> = runCatching {
        client.invokeFoodEntryAnalysis(foodEntryId)
    }
}
