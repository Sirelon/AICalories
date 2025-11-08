package com.sirelon.aicalories.supabase.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ReportAnalysisSummaryResponse(
    @SerialName("id") val id: Long,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("advice") val advice: String? = null,
    @SerialName("quality") val quality: String? = null,
    @SerialName("issues") val issues: List<String>? = emptyList(),
    @SerialName("uncertainties") val uncertainties: List<String>? = emptyList(),
    @SerialName("checklist") val checklist: List<String>? = emptyList(),
    @SerialName("raw_json") val rawJson: JsonElement? = null,
    @SerialName("food_entry_id") val foodEntryId: Long,
)

@Serializable
data class ReportAnalysisEntryResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("desc") val description: String? = null,
    @SerialName("qty_unit") val quantityUnit: String? = null,
    @SerialName("qty_value") val quantityValue: Long? = null,
    @SerialName("kcal") val kcal: Long? = null,
    @SerialName("protein_g") val proteinGrams: Double? = null,
    @SerialName("fat_g") val fatGrams: Double? = null,
    @SerialName("carbs_g") val carbsGrams: Double? = null,
    @SerialName("confidence") val confidence: Double? = null,
    @SerialName("from_image") val fromImage: Boolean = false,
    @SerialName("from_note") val fromNote: Boolean = false,
    @SerialName("photo_index") val photoIndex: Int? = null,
    @SerialName("report_analyse_id") val reportAnalyseId: Long? = null,
)
