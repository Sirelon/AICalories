package com.sirelon.aicalories.features.analyze.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import com.sirelon.aicalories.common.MeasureUnit
import com.sirelon.aicalories.designsystem.ChipData
import com.sirelon.aicalories.designsystem.ChipStyle
import com.sirelon.aicalories.designsystem.templates.MacroStats
import com.sirelon.aicalories.designsystem.templates.NutritionValue
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi
import com.sirelon.aicalories.features.analyze.model.MealEntryUi
import com.sirelon.aicalories.features.analyze.model.MealSummaryUi
import com.sirelon.aicalories.supabase.response.ReportAnalysisEntryResponse
import com.sirelon.aicalories.supabase.response.ReportAnalysisSummaryResponse
import com.sirelon.aicalories.utils.normalizePercentage
import com.sirelon.aicalories.utils.roundToDecimals

class ReportAnalysisUiMapper {

    fun toUi(
        summary: ReportAnalysisSummaryResponse,
        entries: List<ReportAnalysisEntryResponse>,
    ): MealAnalysisUi {
        val summaryUi = MealSummaryUi(
            headline = summary.advice?.trim().orEmpty().ifBlank { null },
            qualityLabel = summary.quality?.trim()?.takeIf { it.isNotEmpty() }
                ?.let { "Overall quality: $it" },
            issues = summary.issues?.filterNotNullOrBlank().orEmpty(),
            checklist = summary.checklist?.filterNotNullOrBlank().orEmpty(),
            uncertainties = summary.uncertainties?.filterNotNullOrBlank().orEmpty(),
        )

        val entryUi = entries.map(::entryToUi)

        return MealAnalysisUi(
            summary = summaryUi,
            entries = entryUi,
        )
    }

    private fun entryToUi(entry: ReportAnalysisEntryResponse): MealEntryUi {
        return MealEntryUi(
            id = entry.id,
            title = entry.name.ifBlank { "Unnamed item" },
            description = entry.description?.trim().takeIf { !it.isNullOrBlank() },
            quantityText = formatQuantity(entry.quantityValue, entry.quantityUnit),
            macroStats = macroStats(entry),
            confidence = confidenceChip(entry.confidence),
            sourceTags = sourceTags(entry),
        )
    }

    private fun macroStats(entry: ReportAnalysisEntryResponse): MacroStats = MacroStats(
        calories = NutritionValue(
            type = MeasureUnit.Kcal,
            value = entry.kcal?.toDouble() ?: 0.0
        ),
        protein = NutritionValue(
            type = MeasureUnit.Grams,
            value = entry.proteinGrams?.roundToDecimals() ?: 0.0
        ),
        carbs = NutritionValue(
            type = MeasureUnit.Grams,
            value = entry.carbsGrams?.roundToDecimals() ?: 0.0
        ),
        fat = NutritionValue(
            type = MeasureUnit.Grams,
            value = entry.fatGrams?.roundToDecimals() ?: 0.0,
        ),
    )

    private fun sourceTags(entry: ReportAnalysisEntryResponse): List<ChipData> =
        buildList {
            if (entry.fromImage) {
                val text = entry.photoIndex?.let { "Photo #${it + 1}" } ?: "From photo"
                add(
                    ChipData(
                        text = text,
                        icon = Icons.Default.Image,
                        style = ChipStyle.Neutral,
                    )
                )
            }
            if (entry.fromNote) {
                add(
                    ChipData(
                        text = "From note",
                        icon = Icons.AutoMirrored.Filled.Note,
                        style = ChipStyle.Neutral,
                    )
                )
            }
        }

    private fun confidenceChip(confidence: Double?): ChipData {
        val percentage = confidence.normalizePercentage()
        val label = percentage.formatConfidence()
        return ChipData(
            text = label,
            icon = Icons.Default.Check,
            style = percentage.confidenceStyle(),
        )
    }

    private fun Double.confidenceStyle(): ChipStyle {
        return when {
            this >= 70 -> ChipStyle.Success
            this <= 25 -> ChipStyle.Error
            else -> ChipStyle.Neutral
        }
    }

    private fun List<String?>.filterNotNullOrBlank(): List<String> =
        mapNotNull { it?.trim() }.filter { it.isNotEmpty() }

    private fun formatQuantity(value: Long?, unit: String?): String? {
        val cleanUnit = unit?.trim().takeUnless { it.isNullOrBlank() }
        return when {
            value == null && cleanUnit == null -> null
            value == null -> cleanUnit
            cleanUnit == null -> value.toString()
            else -> "$value $cleanUnit"
        }
    }

    private fun Double.formatConfidence(): String {
        val rounded = roundToDecimals(0)
        return "$rounded% confidence"
    }
}
