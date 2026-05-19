package com.sirelon.sellsnap.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics
import org.koin.core.module.Module
import org.koin.dsl.module

internal class FirebaseAnalyticsImpl : Analytics {
    private val analytics = Firebase.analytics
    private val crashlytics = Firebase.crashlytics

    override fun logEvent(name: String, params: Map<String, Any>) {
        analytics.logEvent(name, params.ifEmpty { null })
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
        if (userId != null) crashlytics.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        if (value != null) analytics.setUserProperty(name, value)
    }

    override fun recordException(throwable: Throwable, message: String?) {
        if (message != null) crashlytics.log(message)
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }
}

actual val analyticsModule: Module = module {
    single<Analytics> { FirebaseAnalyticsImpl() }
}
