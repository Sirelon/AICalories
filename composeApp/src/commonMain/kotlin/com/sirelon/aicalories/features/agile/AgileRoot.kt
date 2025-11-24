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
import com.sirelon.aicalories.features.agile.capacity.CapacityResultScreen
import com.sirelon.aicalories.features.agile.team.TeamScreen
import com.sirelon.aicalories.features.agile.teamlist.TeamPickerScreen

@Composable
fun AgileRoot(
    onExit: () -> Unit,
) {
    val navBackStack = remember {
        mutableStateListOf<AgileDestination>(AgileDestination.TeamPicker)
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
    val replaceTopDestination: (AgileDestination) -> Unit = { destination ->
        if (navBackStack.isNotEmpty()) {
            navBackStack[navBackStack.lastIndex] = destination
        } else {
            navBackStack.add(destination)
        }
    }
    val removeTopDestination: () -> Unit = {
        navBackStack.removeLastOrNull()
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AgileDestination>()),
        entryProvider = entryProvider<AgileDestination> {
            entry<AgileDestination.TeamPicker> {
                TeamPickerScreen(
                    onBack = onExit,
                    onTeamSelected = { teamId ->
                        pushDestination(AgileDestination.StoryBoard(teamId))
                    },
                    onOpenTeamSettings = { teamId ->
                        pushDestination(AgileDestination.TeamSettings(teamId))
                    },
                )
            }
            entry<AgileDestination.StoryBoard> { destination ->
                AgileScreen(
                    onBack = popDestination,
                    onOpenTeamPicker = { pushDestination(AgileDestination.TeamSwitcher) },
                    onOpenCapacityResult = { teamId ->
                        pushDestination(AgileDestination.CapacityResult(teamId))
                    },
                    teamId = destination.teamId,
                )
            }
            entry<AgileDestination.TeamSwitcher> {
                TeamPickerScreen(
                    onBack = popDestination,
                    onTeamSelected = { teamId ->
                        removeTopDestination()
                        replaceTopDestination(AgileDestination.StoryBoard(teamId))
                    },
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
            entry<AgileDestination.CapacityResult> { destination ->
                CapacityResultScreen(
                    onBack = popDestination,
                    teamId = destination.teamId,
                )
            }
        },
    )
}
