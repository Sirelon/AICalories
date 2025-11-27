package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sirelon.aicalories.features.agile.capacity.CapacityResultScreen
import com.sirelon.aicalories.features.agile.navigation.AgileDestination
import com.sirelon.aicalories.features.agile.team.Team
import com.sirelon.aicalories.features.agile.team.TeamScreen
import com.sirelon.aicalories.features.agile.teamlist.TeamPickerScreen
import com.sirelon.aicalories.navigation.ThreePaneSceneStrategy
import com.sirelon.aicalories.navigation.rememberListDetailSceneStrategy
import com.sirelon.aicalories.navigation.rememberThreePaneSceneStrategy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgileRoot(
    onExit: () -> Unit,
) {

    val navBackStack = remember {
        val teamId = Team.DEFAULT_TEAM_ID
        mutableStateListOf(
            AgileDestination.CapacityResult(teamId),
            AgileDestination.StoryBoard(teamId),
            AgileDestination.TeamPicker,
        )
    }
    val listDetailStrategy = rememberThreePaneSceneStrategy<AgileDestination>()
        .then(
            rememberListDetailSceneStrategy<AgileDestination>()
        )


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

    val (capacityScreenVisible, setCapacityVisible) = remember { mutableStateOf(false) }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        onBack = { navBackStack.removeLastOrNull() },
        sceneStrategy = listDetailStrategy,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<AgileDestination>(),
        ),
        entryProvider = entryProvider {
            entry<AgileDestination.TeamPicker>(
                metadata = ThreePaneSceneStrategy.firstPane()
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
            entry<AgileDestination.TeamSettings>(
                metadata = ThreePaneSceneStrategy.secondPane()
            ) { destination ->
                TeamScreen(
                    onBack = popDestination,
                    teamId = destination.teamId,
                )
            }
            entry<AgileDestination.StoryBoard>(
                metadata = ThreePaneSceneStrategy.secondPane()
            ) { destination ->
                AgileScreen(
                    onBack = popDestination,
                    onOpenTeamPicker = { replaceTopDestination(AgileDestination.TeamPicker) },
                    onOpenCapacityResult = { teamId ->
                        pushDestination(AgileDestination.CapacityResult(teamId))
                    },
                    teamId = destination.teamId,
                    showCalculateCapacityButton = !capacityScreenVisible,
                )
            }
            entry<AgileDestination.CapacityResult>(
                metadata = ThreePaneSceneStrategy.thirdPane()
            ) { destination ->

                DisposableEffect(null) {
                    setCapacityVisible(true)
                    onDispose {
                        setCapacityVisible(false)
                    }
                }

                CapacityResultScreen(
                    onBack = popDestination,
                    teamId = destination.teamId,
                )
            }
        }
    )
}