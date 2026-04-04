package com.sirelon.aicalories.features.analyze.data

import com.sirelon.aicalories.features.media.upload.UploadedFile
import com.sirelon.aicalories.supabase.SupabaseClient
import com.sirelon.aicalories.supabase.model.AnalyseReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class AnalyzeRepository(
    private val client: SupabaseClient,
) {

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

            if (files.isNotEmpty() && fileIds.isEmpty()) {
                error("Files were not uploaded to storage. Please try again.")
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
