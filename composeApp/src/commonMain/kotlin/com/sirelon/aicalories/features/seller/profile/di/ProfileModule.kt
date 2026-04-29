package com.sirelon.aicalories.features.seller.profile.di

import com.sirelon.aicalories.features.seller.location.createLocationProvider
import com.sirelon.aicalories.features.seller.location.data.LocationStore
import com.sirelon.aicalories.features.seller.location.data.LocationRepository
import com.sirelon.aicalories.features.seller.profile.data.SellerAccountRepository
import com.sirelon.aicalories.features.seller.profile.presentation.ProfileViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    single { createLocationProvider() }
    singleOf<LocationStore>(::LocationStore)
    singleOf(::LocationRepository)
    singleOf(::SellerAccountRepository)
    viewModelOf(::ProfileViewModel)
}
