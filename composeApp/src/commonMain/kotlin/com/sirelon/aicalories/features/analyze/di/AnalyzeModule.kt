package com.sirelon.aicalories.features.analyze.di

import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val analyzeModule = module {
    factoryOf(::AnalyzeRepository)
    viewModelOf(::AnalyzeViewModel)
}
