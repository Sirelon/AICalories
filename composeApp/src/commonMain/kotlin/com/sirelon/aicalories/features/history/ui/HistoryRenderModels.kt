package com.sirelon.aicalories.features.history.ui

/**
 * Collection of lightweight render models that mirrors the Supabase history schema while
 * exposing pre-formatted strings for the UI layer. They intentionally avoid business logic so
 * the screen can be previewed without a data source.
 */
data class HistoryScreenRenderModel(
    val header: HistoryHeaderRenderModel = HistoryHeaderRenderModel(),
    val weeklySummary: WeeklyCaloriesRenderModel? = null,
    val groupedEntries: List<HistoryGroupRenderModel> = emptyList(),
    val highlightedEntryId: Long? = null,
    val emptyState: HistoryEmptyStateRenderModel? = null,
)

data class HistoryHeaderRenderModel(
    val title: String = "",
    val subtitle: String? = null,
    val insights: List<String> = emptyList(),
)

data class WeeklyCaloriesRenderModel(
    val title: String,
    val totalLabel: String,
    val changeLabel: String? = null,
    val points: List<CaloriePointRenderModel>,
    val targetLabel: String? = null,
)

data class CaloriePointRenderModel(
    val id: String,
    val dayLabel: String,
    val caloriesValue: Int,
    val caloriesLabel: String,
)

data class HistoryGroupRenderModel(
    val groupId: String,
    val dayLabel: String,
    val entries: List<HistoryEntryRenderModel>,
)

data class HistoryEntryRenderModel(
    val id: Long,
    val dateLabel: String,
    val timeLabel: String,
    val caloriesLabel: String? = null,
    val note: String? = null,
    val attachments: List<HistoryAttachmentRenderModel> = emptyList(),
    val foods: List<HistoryFoodRenderModel> = emptyList(),
    val macros: MacroBreakdownRenderModel? = null,
    val summary: HistoryReportSummaryRenderModel? = null,
    val tags: List<String> = emptyList(),
    val photoCountLabel: String? = null,
    val confidenceLabel: String? = null,
)

data class HistoryAttachmentRenderModel(
    val id: String,
    val previewUrl: String?,
    val description: String? = null,
)

data class HistoryFoodRenderModel(
    val id: String,
    val title: String,
    val description: String? = null,
    val quantityLabel: String? = null,
    val caloriesLabel: String? = null,
    val macroLabel: String? = null,
    val confidenceLabel: String? = null,
    val fromImage: Boolean = false,
    val fromNote: Boolean = false,
)

data class MacroBreakdownRenderModel(
    val calories: String,
    val protein: String,
    val fat: String,
    val carbs: String,
)

data class HistoryReportSummaryRenderModel(
    val advice: String? = null,
    val qualityLabel: String? = null,
    val issues: List<String> = emptyList(),
    val uncertainties: List<String> = emptyList(),
    val checklist: List<String> = emptyList(),
)

data class HistoryEmptyStateRenderModel(
    val title: String,
    val description: String,
    val actionLabel: String? = null,
)
