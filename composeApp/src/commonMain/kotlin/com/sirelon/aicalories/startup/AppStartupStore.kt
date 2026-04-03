package com.sirelon.aicalories.startup

import com.sirelon.aicalories.datastore.createKeyValueStore

class AppStartupStore {
    private val store = createKeyValueStore("app_startup")

    suspend fun hasSeenOnboarding(): Boolean =
        store.getString(KEY_ONBOARDING_SEEN) != null

    suspend fun markOnboardingSeen() {
        store.putString(KEY_ONBOARDING_SEEN, "true")
    }

    private companion object {
        const val KEY_ONBOARDING_SEEN = "has_seen_onboarding"
    }
}
