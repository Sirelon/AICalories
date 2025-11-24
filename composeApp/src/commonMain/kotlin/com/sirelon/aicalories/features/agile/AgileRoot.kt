package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sirelon.aicalories.features.agile.navigation.AgileDestination
import com.sirelon.aicalories.features.agile.team.TeamScreen

@Composable
fun AgileRoot(
    onExit: () -> Unit,
) {
    val navBackStack = remember {
        mutableStateListOf<AgileDestination>(AgileDestination.StoryBoard)
    }

    val popDestination: () -> Unit = {
        if (navBackStack.size > 1) {
            navBackStack.removeLastOrNull()
        } else {
            onExit()
        }
    }
    val pushDestination: (AgileDestination) -> Unit = { destination ->
        navBackStack.add(destination)
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AgileDestination>()),
        entryProvider = entryProvider<AgileDestination> {
            entry<AgileDestination.StoryBoard> {
                AgileScreen(
                    onBack = popDestination,
                    onOpenTeamSettings = { teamId ->
                        pushDestination(AgileDestination.TeamSettings(teamId))
                    },
                )
            }
            entry<AgileDestination.TeamSettings> { destination ->
                TeamScreen(
                    onBack = popDestination,
                    teamId = destination.teamId,
                )
            }
        },
    )
}