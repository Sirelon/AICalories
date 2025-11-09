package com.sirelon.aicalories.features.analyze.di

import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import com.sirelon.aicalories.features.analyze.data.ReportAnalysisUiMapper
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import com.sirelon.aicalories.features.media.imageFormatConverter
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val analyzeModule = module {
    single { ReportAnalysisUiMapper() }
    factoryOf(::AnalyzeRepository)
    single { imageFormatConverter() }
    viewModelOf(::AnalyzeViewModel)
}
