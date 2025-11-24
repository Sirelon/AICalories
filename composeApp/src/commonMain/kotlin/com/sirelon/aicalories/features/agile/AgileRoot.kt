package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sirelon.aicalories.features.agile.capacity.CapacityResultScreen
import com.sirelon.aicalories.features.agile.navigation.AgileDestination
import com.sirelon.aicalories.features.agile.team.TeamScreen
import com.sirelon.aicalories.features.agile.teamlist.TeamPickerScreen
import com.sirelon.aicalories.navigation.ListDetailSceneStrategy
import com.sirelon.aicalories.navigation.rememberListDetailSceneStrategy

@Composable
fun AgileRoot(
    onExit: () -> Unit,
) {

    val navBackStack = remember {
        mutableStateListOf<AgileDestination>(AgileDestination.TeamPicker)
    }

    val listDetailStrategy = rememberListDetailSceneStrategy<AgileDestination>()

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

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        onBack = { navBackStack.removeLastOrNull() },
        sceneStrategy = listDetailStrategy,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AgileDestination>()),
        entryProvider = entryProvider<AgileDestination> {
            entry<AgileDestination.TeamPicker>(
                metadata = ListDetailSceneStrategy.listPane()
            ) {
                TeamPickerScreen(
                    onBack = onExit,
                    onTeamSelected = { teamId ->
                        pushDestination(AgileDestination.StoryBoard(teamId))
                        replaceTopDestination(AgileDestination.StoryBoard(teamId))
                    },
                    onOpenTeamSettings = { teamId ->
                        pushDestination(AgileDestination.TeamSettings(teamId))
                    },
                )
            }

            entry<AgileDestination.StoryBoard>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { destination ->
                AgileScreen(
                    onBack = popDestination,
                    onOpenTeamPicker = { pushDestination(AgileDestination.TeamPicker) },
                    onOpenCapacityResult = { teamId ->
                        pushDestination(AgileDestination.CapacityResult(teamId))
                    },
                    teamId = destination.teamId,
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
        }
    )
}