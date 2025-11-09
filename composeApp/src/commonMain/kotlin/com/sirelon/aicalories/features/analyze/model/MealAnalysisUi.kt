package com.sirelon.aicalories.features.analyze.model

import com.sirelon.aicalories.designsystem.ChipData
import com.sirelon.aicalories.designsystem.templates.MacroStats

data class MealAnalysisUi(
    val summary: MealSummaryUi,
    val entries: List<MealEntryUi>,
) {
    val hasContent: Boolean
        get() = summary.hasContent || entries.isNotEmpty()
}

data class MealSummaryUi(
    val headline: String?,
    val qualityLabel: String?,
    val issues: List<String>,
    val checklist: List<String>,
    val uncertainties: List<String>,
) {
    val hasContent: Boolean
        get() = !headline.isNullOrBlank() ||
            !qualityLabel.isNullOrBlank() ||
            issues.isNotEmpty() ||
            checklist.isNotEmpty() ||
            uncertainties.isNotEmpty()
}

data class MealEntryUi(
    val id: String,
    val title: String,
    val description: String?,
    val quantityText: String?,
    val macroStats: MacroStats,
    val confidence: ChipData,
    val sourceTags: List<ChipData>,
)