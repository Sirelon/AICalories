package com.sirelon.aicalories.features.agile.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.sirelon.aicalories.designsystem.AppChip
import com.sirelon.aicalories.designsystem.AppChipDefaults
import com.sirelon.aicalories.designsystem.AppDimens
import kotlin.math.roundToInt

@Composable
fun TeamSummary(
    team: Team,
    storiesCount: Int? = null,
    ticketsCount: Int? = null,
) {
    val boundedRisk = team.riskFactor.coerceIn(0.0, 1.0)
    val riskPercentage = (boundedRisk * 100).roundToInt()
    val pessimisticCapacity = (team.capacity - team.capacity * boundedRisk)
        .roundToInt()
        .coerceAtLeast(0)
    val optimisticCapacity = (team.capacity + team.capacity * boundedRisk)
        .roundToInt()
    val neutralChipColors = AppChipDefaults.neutralColors()
    val capacityChipColors = AppChipDefaults.capacityColors(
        fitsPessimistic = riskPercentage < 60,
        fitsOptimistic = riskPercentage < 25,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.Spacing.xl),
    ) {
        Text(
            text = "${team.name} overview",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            AppChip(
                text = team.name,
                icon = Icons.Outlined.PeopleOutline,
                colors = AppChipDefaults.primaryColors(),
            )
            AppChip(
                text = "${team.peopleCount} people",
                icon = Icons.Outlined.Group,
                colors = neutralChipColors,
            )
            AppChip(
                text = "Capacity ${team.capacity}",
                icon = Icons.Outlined.Event,
                colors = capacityChipColors,
            )
            AppChip(
                text = "Risk ±$riskPercentage%",
                icon = Icons.Outlined.Warning,
                colors = AppChipDefaults.errorColors(),
            )
            AppChip(
                text = "Range $pessimisticCapacity-$optimisticCapacity",
                icon = Icons.Outlined.Cached,
                colors = AppChipDefaults.primaryColors(),
            )
            storiesCount?.let { count ->
                AppChip(
                    text = "$count user stories",
                    icon = Icons.Outlined.Description,
                    colors = neutralChipColors,
                )
            }
            ticketsCount?.let { count ->
                AppChip(
                    text = "$count tickets",
                    icon = Icons.Outlined.BugReport,
                    colors = neutralChipColors,
                )
            }
        }
    }
}
