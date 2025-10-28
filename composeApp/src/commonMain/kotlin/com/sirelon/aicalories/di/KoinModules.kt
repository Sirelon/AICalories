package com.sirelon.aicalories.di

import com.sirelon.aicalories.Greeting
import com.sirelon.aicalories.features.analyze.di.analyzeModule
import com.sirelon.aicalories.network.ApiTokenProvider
import com.sirelon.aicalories.network.createHttpClient
import org.koin.dsl.module

val appModule = module {
    includes(analyzeModule)
    single { Greeting() }
}

val networkModule = module {
    single { ApiTokenProvider() }
    single { createHttpClient(get()) }
}
