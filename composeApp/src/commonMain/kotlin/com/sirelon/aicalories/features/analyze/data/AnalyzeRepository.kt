package com.sirelon.aicalories.features.analyze.data

import io.ktor.client.HttpClient
import kotlinx.coroutines.delay

class AnalyzeRepository(
    private val api: HttpClient,
) {
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
