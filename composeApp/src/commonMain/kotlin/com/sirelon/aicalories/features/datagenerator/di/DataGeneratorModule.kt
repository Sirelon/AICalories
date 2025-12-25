package com.sirelon.aicalories.features.datagenerator.di

import com.sirelon.aicalories.features.datagenerator.data.RandomDataGenerator
import com.sirelon.aicalories.features.datagenerator.presentation.DataGeneratorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataGeneratorModule = module {
    factoryOf(::RandomDataGenerator)
    viewModelOf(::DataGeneratorViewModel)
}
