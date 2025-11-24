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

@Composable
fun TeamSummary(team: Team) {
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
        }
    }
}