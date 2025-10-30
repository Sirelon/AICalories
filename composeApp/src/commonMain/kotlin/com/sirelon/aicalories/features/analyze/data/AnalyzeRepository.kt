package com.sirelon.aicalories.features.analyze.data

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.readByteArray
import com.sirelon.aicalories.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AnalyzeRepository(
    private val client: SupabaseClient,
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

    suspend fun analyzeDescription(description: String): Result<AnalyzeResult> {
        delay(450)

        return Result.success(
            AnalyzeResult(
                summary = "Nutritional insights will appear here soon.",
                recommendation = "Keep tracking meals like \"$description\" to unlock analysis.",
            ),
        )
    }
}
