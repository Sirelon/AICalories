package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.sirelon.aicalories.features.agile.capacity.CapacityResultScreen
import com.sirelon.aicalories.features.agile.navigation.AgileDestination
import com.sirelon.aicalories.features.agile.team.TeamScreen
import com.sirelon.aicalories.features.agile.teamlist.TeamPickerScreen

@Composable
fun AgileRoot(
    onExit: () -> Unit,
) {
    val navBackStack = remember {
        mutableStateListOf<AgileDestination>(AgileDestination.TeamPicker)
    }
    val windowInfo = LocalWindowInfo.current
    val isWide = windowInfo.containerDpSize.width >= 960.dp

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

    val current = navBackStack.last()
    if (isWide && current is AgileDestination.StoryBoard) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(0.42f)) {
                TeamPickerScreen(
                    onBack = onExit,
                    onTeamSelected = { teamId ->
                        replaceTopDestination(AgileDestination.StoryBoard(teamId))
                    },
                    onOpenTeamSettings = { teamId ->
                        pushDestination(AgileDestination.TeamSettings(teamId))
                    },
                )
            }
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(0.58f),
            ) {
                AgileScreen(
                    onBack = popDestination,
                    onOpenTeamPicker = { pushDestination(AgileDestination.TeamSwitcher) },
                    onOpenCapacityResult = { teamId ->
                        pushDestination(AgileDestination.CapacityResult(teamId))
                    },
                    teamId = current.teamId,
                )
            }
        }
    } else {
        when (current) {
            AgileDestination.TeamPicker -> TeamPickerScreen(
                onBack = onExit,
                onTeamSelected = { teamId ->
                    pushDestination(AgileDestination.StoryBoard(teamId))
                },
                onOpenTeamSettings = { teamId ->
                    pushDestination(AgileDestination.TeamSettings(teamId))
                },
            )

            AgileDestination.TeamSwitcher -> TeamPickerScreen(
                onBack = popDestination,
                onTeamSelected = { teamId ->
                    removeTopDestination()
                    replaceTopDestination(AgileDestination.StoryBoard(teamId))
                },
                onOpenTeamSettings = { teamId ->
                    pushDestination(AgileDestination.TeamSettings(teamId))
                },
            )

            is AgileDestination.StoryBoard -> AgileScreen(
                onBack = popDestination,
                onOpenTeamPicker = { pushDestination(AgileDestination.TeamSwitcher) },
                onOpenCapacityResult = { teamId ->
                    pushDestination(AgileDestination.CapacityResult(teamId))
                },
                teamId = current.teamId,
            )

            is AgileDestination.TeamSettings -> TeamScreen(
                onBack = popDestination,
                teamId = current.teamId,
            )

            is AgileDestination.CapacityResult -> CapacityResultScreen(
                onBack = popDestination,
                teamId = current.teamId,
            )
        }
    }
}
