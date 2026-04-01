package com.sirelon.aicalories.features.seller.ad.generate_ad.di

import com.sirelon.aicalories.features.seller.ad.generate_ad.GenerateAdViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val generateAdModule = module {
    viewModelOf(::GenerateAdViewModel)
}
