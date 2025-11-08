package com.sirelon.aicalories.features.analyze.model

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
    val macroStats: List<MacroStatUi>,
    val confidenceText: String?,
    val sourceTags: List<String>,
)

data class MacroStatUi(
    val label: String,
    val value: String,
)
