package com.sirelon.aicalories.features.agile.di

import com.sirelon.aicalories.features.agile.presentation.AgileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val agileModule = module {
    viewModelOf(::AgileViewModel)
}
