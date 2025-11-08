package com.sirelon.aicalories.features.history.ui

import com.sirelon.aicalories.designsystem.ChipData

data class HistoryScreenRenderModel(
    val header: HistoryHeaderRenderModel,
    val insights: List<ChipData> = emptyList(),
    val weeklySummary: WeeklyCaloriesRenderModel? = null,
    val groupedEntries: List<HistoryGroupRenderModel> = emptyList(),
    val highlightedEntryId: Long? = null,
    val emptyState: HistoryEmptyStateRenderModel? = null,
)

data class HistoryHeaderRenderModel(
    val title: String,
    val subtitle: String,
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
    val summary: HistoryReportSummaryRenderModel,
    val tags: List<ChipData> = emptyList(),
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
    val qualityLabel: ChipData,
    val issues: List<ChipData> = emptyList(),
    val uncertainties: List<ChipData> = emptyList(),
    val checklist: List<ChipData> = emptyList(),
)

data class HistoryEmptyStateRenderModel(
    val title: String,
    val description: String,
    val actionLabel: String? = null,
)
