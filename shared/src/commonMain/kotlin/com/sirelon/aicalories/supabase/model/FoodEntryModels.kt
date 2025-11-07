package com.sirelon.aicalories.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FoodEntryInsert(
    @SerialName("note")
    val note: String? = null,
)

@Serializable
internal data class FoodEntryRecord(
    @SerialName("id")
    val id: Long,
)

@Serializable
internal data class FoodEntryToFileInsert(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("food_entry_id")
    val foodEntryId: Long,
)

@Serializable
internal data class StorageObjectRecord(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
)
