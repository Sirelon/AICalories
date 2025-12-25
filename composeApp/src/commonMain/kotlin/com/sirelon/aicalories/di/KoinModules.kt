package com.sirelon.aicalories.di

import com.sirelon.aicalories.Greeting
import com.sirelon.aicalories.features.agile.di.agileModule
import com.sirelon.aicalories.features.analyze.di.analyzeModule
import com.sirelon.aicalories.features.datagenerator.di.dataGeneratorModule
import com.sirelon.aicalories.features.history.di.historyModule
import com.sirelon.aicalories.network.ApiTokenProvider
import com.sirelon.aicalories.network.createHttpClient
import com.sirelon.aicalories.supabase.SupabaseClient
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf

val appModule = module {
    includes(agileModule, analyzeModule, historyModule, dataGeneratorModule)
    single { Greeting() }
}

val networkModule = module {
    single { ApiTokenProvider() }
    single { createHttpClient(get()) }
    singleOf(::SupabaseClient)
}
