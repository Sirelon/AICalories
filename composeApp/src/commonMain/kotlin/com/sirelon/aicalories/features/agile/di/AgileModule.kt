package com.sirelon.aicalories.features.agile.di

import com.sirelon.aicalories.features.agile.EstimationCalculator
import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.agile.capacity.CapacityResultViewModel
import com.sirelon.aicalories.features.agile.presentation.AgileViewModel
import com.sirelon.aicalories.features.agile.team.TeamViewModel
import com.sirelon.aicalories.features.agile.teamlist.TeamPickerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val agileModule = module {
    singleOf(::AgileRepository)

    factoryOf(::EstimationCalculator)

    viewModelOf(::AgileViewModel)
    viewModelOf(::TeamViewModel)
    viewModelOf(::TeamPickerViewModel)
    viewModelOf(::CapacityResultViewModel)
}
