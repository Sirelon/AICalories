package com.sirelon.aicalories.features.seller.location

import android.content.Context

object AndroidAppContextHolder {
    var applicationContext: Context? = null
        private set

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
}
