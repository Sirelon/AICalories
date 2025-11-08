package com.sirelon.aicalories.features.analyze.data

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.readByteArray
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi
import com.sirelon.aicalories.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AnalyzeRepository(
    private val client: SupabaseClient,
    private val mapper: ReportAnalysisUiMapper,
) {

    @OptIn(ExperimentalUuidApi::class)
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

    fun observeAnalysis(foodEntryId: Long): Flow<MealAnalysisUi?> {
        val summaryFlow = client.observeReportSummary(foodEntryId)
        val entriesFlow = summaryFlow
            .map { it?.id }
            .distinctUntilChanged()
            .flatMapLatest { summaryId ->
                if (summaryId == null) {
                    flowOf(emptyList())
                } else {
                    client.observeReportEntries(summaryId)
                }
            }

        return summaryFlow
            .combine(entriesFlow) { summary, entries ->
                summary?.let {
                    mapper.toUi(
                        summary = it,
                        entries = entries,
                    )
                }
            }
            .onStart { emit(null) }
    }

    suspend fun analyzeDescription(prompt: String): Result<Long> {
        delay(450)

        return Result.success(System.currentTimeMillis())
    }
}
