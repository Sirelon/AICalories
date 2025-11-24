package com.sirelon.aicalories.features.agile.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(team.name) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.PeopleOutline,
                        contentDescription = null,
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    leadingIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("${team.peopleCount} people") },
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Capacity ${team.capacity}") },
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Risk ±$riskPercentage%") },
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Range $pessimisticCapacity-$optimisticCapacity") },
            )
            storiesCount?.let { count ->
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("$count user stories") },
                )
            }
            ticketsCount?.let { count ->
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("$count tickets") },
                )
            }
        }
    }
}
