package com.sirelon.aicalories.features.sellerauth.data

object OlxAuthCallbackBridge {
    private var cachedUrl: String? = null

    var listener: ((String) -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                cachedUrl?.let { url ->
                    value.invoke(url)
                    cachedUrl = null
                }
            }
        }

    fun onNewUri(url: String) {
        cachedUrl = url
        listener?.let { activeListener ->
            activeListener.invoke(url)
            cachedUrl = null
        }
    }

    fun publishCallback(url: String) {
        onNewUri(url)
    }
}
