package com.sirelon.aicalories.features.seller.ad.preview_ad.di

import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdViewModel
import com.sirelon.aicalories.features.seller.location.createLocationProvider
import com.sirelon.aicalories.features.seller.location.data.LocationRepository
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val previewAdModule = module {
    single { createLocationProvider() }
    single { LocationRepository(get(), get()) }
    viewModelOf(::PreviewAdViewModel)
}
