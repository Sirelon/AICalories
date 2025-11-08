package com.sirelon.aicalories.features.analyze.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import com.sirelon.aicalories.designsystem.ChipData
import com.sirelon.aicalories.features.analyze.model.MacroStatUi
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi
import com.sirelon.aicalories.features.analyze.model.MealEntryUi
import com.sirelon.aicalories.features.analyze.model.MealSummaryUi
import com.sirelon.aicalories.supabase.response.ReportAnalysisEntryResponse
import com.sirelon.aicalories.supabase.response.ReportAnalysisSummaryResponse
import kotlin.math.round

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
            macroStats = listOf(
                MacroStatUi(
                    label = "Calories",
                    value = entry.kcal?.let { "$it kcal" } ?: PLACEHOLDER),
                MacroStatUi(label = "Protein", value = entry.proteinGrams.formatMacro("g")),
                MacroStatUi(label = "Carbs", value = entry.carbsGrams.formatMacro("g")),
                MacroStatUi(label = "Fat", value = entry.fatGrams.formatMacro("g")),
            ),
            confidenceText = confidenceChip(entry.confidence),
            sourceTags = sourceTags(entry),
        )
    }

    private fun sourceTags(entry: ReportAnalysisEntryResponse): List<ChipData> = buildList {
        if (entry.fromImage) {
            val text = entry.photoIndex?.let { "Photo #${it + 1}" } ?: "From photo"
            ChipData(text = text, icon = Icons.Default.Image)
        }
        if (entry.fromNote) {
            ChipData(text = "From note", icon = Icons.AutoMirrored.Filled.Note)
        }
    }

    private fun confidenceChip(confidence: Double?): ChipData? =
        confidence.formatConfidence()?.let {
            ChipData(
                text = it,
                icon = Icons.Default.Check,
            )
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

    private fun Double?.formatMacro(suffix: String): String =
        this?.let { "${it.roundToDecimals()} $suffix" } ?: PLACEHOLDER

    private fun Double.roundToDecimals(decimals: Int = 1): String {
        if (decimals <= 0) {
            return round(this).toInt().toString()
        }
        val factor = (1..decimals).fold(1.0) { acc, _ -> acc * 10.0 }
        val rounded = round(this * factor) / factor
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            rounded.toString()
        }
    }

    private fun Double?.formatConfidence(): String? {
        if (this == null) return null
        val percentage = if (this <= 1.0) this * 100 else this
        val normalized = percentage.coerceIn(0.0, 100.0)
        val rounded = normalized.roundToDecimals(0)
        return "$rounded% confidence"
    }
}

private const val PLACEHOLDER = "—"
