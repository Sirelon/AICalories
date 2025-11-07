package com.sirelon.aicalories.features.history.di

import com.sirelon.aicalories.features.history.presentation.HistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val historyModule = module {
    viewModelOf(::HistoryViewModel)
}
