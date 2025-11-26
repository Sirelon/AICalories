package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sirelon.aicalories.features.agile.capacity.CapacityResultScreen
import com.sirelon.aicalories.features.agile.navigation.AgileDestination
import com.sirelon.aicalories.features.agile.team.TeamScreen
import com.sirelon.aicalories.features.agile.teamlist.TeamPickerScreen
import com.sirelon.aicalories.navigation.ListDetailSceneStrategy
import com.sirelon.aicalories.navigation.ThreePaneSceneStrategy
import com.sirelon.aicalories.navigation.rememberListDetailSceneStrategy
import com.sirelon.aicalories.navigation.rememberThreePaneSceneStrategy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgileRoot(
    onExit: () -> Unit,
) {

    val navBackStack = remember {
        mutableStateListOf<AgileDestination>(AgileDestination.TeamPicker)
    }

//    val listDetailStrategy = rememberListDetailSceneStrategy<AgileDestination>()
//        .then(
//            rememberThreePaneSceneStrategy<AgileDestination>()
//        )
    val listDetailStrategy = rememberListDetailSceneStrategy<AgileDestination>()
        .then(
            rememberThreePaneSceneStrategy()
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

    val currentDestination = navBackStack.last()

    val showCalculateCapacityButton = remember(navBackStack) {
        navBackStack.none { it is AgileDestination.CapacityResult }
    }

    Scaffold(
        topBar = {
            if (currentDestination != AgileDestination.TeamPicker) {
                TopAppBar(
                    title = { Text(currentDestination.title) },
                    navigationIcon = {
                        IconButton(onClick = popDestination) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            backStack = navBackStack,
            onBack = { navBackStack.removeLastOrNull() },
            sceneStrategy = listDetailStrategy,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AgileDestination>()),
            entryProvider = entryProvider {
                entry<AgileDestination.TeamPicker>(
                    metadata = ListDetailSceneStrategy.listPane() + ThreePaneSceneStrategy.firstPane()
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
                    metadata = ListDetailSceneStrategy.detailPane()
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
                    )
                }
                entry<AgileDestination.CapacityResult>(
                    metadata = ThreePaneSceneStrategy.thirdPane()
                ) { destination ->
                    CapacityResultScreen(
                        onBack = popDestination,
                        teamId = destination.teamId,
                    )
                }
            }
        )
    }
}