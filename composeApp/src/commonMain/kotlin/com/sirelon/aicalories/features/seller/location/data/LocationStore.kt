package com.sirelon.aicalories.features.seller.location.data

import com.sirelon.aicalories.datastore.KeyValueStore
import com.sirelon.aicalories.datastore.createKeyValueStore
import com.sirelon.aicalories.features.seller.location.OlxLocation
import kotlinx.serialization.json.Json

class LocationStore internal constructor(
    private val storage: KeyValueStore,
) {
    constructor() : this(createKeyValueStore("olx_location"))

    suspend fun read(): OlxLocation? =
        storage.getString(KEY)?.let { json.decodeFromString<OlxLocation>(it) }

    suspend fun write(location: OlxLocation) {
        storage.putString(KEY, json.encodeToString(location))
    }

    suspend fun clear() {
        storage.remove(KEY)
    }

    private companion object {
        const val KEY = "selected_location"
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }
}
