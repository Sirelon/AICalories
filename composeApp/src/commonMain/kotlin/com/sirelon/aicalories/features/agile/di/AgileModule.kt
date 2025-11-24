package com.sirelon.aicalories.features.agile.di

import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.agile.presentation.AgileViewModel
import com.sirelon.aicalories.features.agile.team.TeamViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val agileModule = module {
    singleOf(::AgileRepository)
    viewModelOf(::AgileViewModel)
    viewModel { (teamId: Int) ->
        TeamViewModel(
            teamId = teamId,
            repository = get(),
        )
    }
}
