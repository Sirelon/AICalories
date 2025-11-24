package com.sirelon.aicalories.features.agile.capacity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.features.agile.Estimation
import com.sirelon.aicalories.features.agile.EstimationResult
import com.sirelon.aicalories.features.agile.FeasibleTicketVariant
import com.sirelon.aicalories.features.agile.capacity.CapacityResultContract.CapacityResultEvent
import com.sirelon.aicalories.features.agile.capacity.CapacityResultContract.CapacityResultState
import com.sirelon.aicalories.features.agile.code
import com.sirelon.aicalories.features.agile.color
import com.sirelon.aicalories.features.agile.estimationCalculatorExample
import com.sirelon.aicalories.features.agile.icon
import com.sirelon.aicalories.features.agile.model.Ticket
import com.sirelon.aicalories.features.agile.team.Team
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

@Composable
internal fun CapacityResultScreen(
    onBack: () -> Unit,
    teamId: Int,
) {
    val viewModel: CapacityResultViewModel = koinViewModel(parameters = { parametersOf(teamId) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    CapacityResultContent(
        state = state,
        onDismiss = onBack,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun CapacityResultContent(
    state: CapacityResultState,
    onDismiss: () -> Unit,
    onEvent: (CapacityResultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val result = state.result

    Surface(
        modifier = modifier.shadow(elevation = 8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                AppLargeAppBar(
                    title = "Capacity result",
                    subtitle = state.team?.name ?: "Estimation outcome",
                    onBack = onDismiss,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            if (result == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Crunching numbers...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = AppDimens.Spacing.m),
                    )
                    TextButton(onClick = { onEvent(CapacityResultEvent.Refresh) }) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing.xl3),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
                ) {
                    item {
                        ResultSummaryCard(
                            result = result,
                            team = state.team,
                        )
                    }

                    item {
                        FeasibleVariantsSection(
                            result = result,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSummaryCard(
    result: EstimationResult,
    team: Team?,
) {
    val remaining = result.capacity - result.totalEffort
    val isOverCapacity = remaining < 0
    val statusColor = if (result.canCloseAll) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val statusText = if (result.canCloseAll) {
        if (remaining == 0) {
            "Exact fit to the available capacity."
        } else {
            "Everything fits this sprint."
        }
    } else {
        "You need ${abs(remaining)} more points."
    }
    val detailText = if (result.canCloseAll) {
        if (remaining > 0) {
            "Total effort ${result.totalEffort} of ${result.capacity} capacity — $remaining points free."
        } else {
            "Total effort ${result.totalEffort} matches the ${result.capacity} capacity."
        }
    } else {
        "Scope effort is ${result.totalEffort} with capacity ${result.capacity}."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = statusColor,
            )
            Text(
                text = detailText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
            ) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Capacity ${result.capacity}") },
                )
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Effort ${result.totalEffort}") },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        disabledLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                val balanceLabel = if (isOverCapacity) {
                    "Over by ${abs(remaining)}"
                } else {
                    "Remaining $remaining"
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(balanceLabel) },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = if (isOverCapacity) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        disabledLabelColor = if (isOverCapacity) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    ),
                )
                if (result.totalVariants > 0) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("${result.totalVariants} variants") },
                    )
                }
                team?.peopleCount?.takeIf { it > 0 }?.let { people ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("$people people") },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeasibleVariantsSection(
    result: EstimationResult,
) {
    val sortedVariants = remember(result) {
        result.feasibleVariants.sortedWith(variantDisplayComparator)
    }
    val totalVariants = result.totalVariants.takeIf { it > 0 } ?: sortedVariants.size
    var showAll by rememberSaveable { mutableStateOf(false) }

    val maxVisible = if (showAll) MAX_VISIBLE_VARIANTS else COLLAPSED_VARIANTS
    val variantsToDisplay = sortedVariants.take(maxVisible)
    val hasHidden = totalVariants > variantsToDisplay.size
    val isCapped = totalVariants > MAX_VISIBLE_VARIANTS

    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
        ) {
            Text(
                text = "Feasible ticket variants",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = "Sorted by best fit to the available capacity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (totalVariants == 0 || sortedVariants.isEmpty()) {
            Text(
                text = if (result.totalEffort == 0) {
                    "Add tickets to see how they stack against the current capacity."
                } else {
                    "No combination of tickets fits the current capacity. Trim scope or increase capacity."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            return@Column
        }

        variantsToDisplay.forEachIndexed { index, variant ->
            VariantCard(
                index = index,
                variant = variant,
                capacity = result.capacity,
            )
        }

        if (hasHidden) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
            ) {
                Text(
                    text = if (isCapped && showAll) {
                        "Showing best ${variantsToDisplay.size} of $totalVariants variants (top $MAX_VISIBLE_VARIANTS kept for readability)."
                    } else if (showAll || !hasHidden) {
                        "Showing best ${variantsToDisplay.size} of $totalVariants variants."
                    } else {
                        "Showing top $COLLAPSED_VARIANTS of $totalVariants variants."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) "Collapse" else "Show more")
                }
            }
        }
    }
}

@Composable
private fun VariantCard(
    index: Int,
    variant: FeasibleTicketVariant,
    capacity: Int,
) {
    val slack = capacity - variant.totalEffort
    val slackLabel = when {
        slack == 0 -> "Exact fit"
        slack > 0 -> "$slack free"
        else -> "Over by ${abs(slack)}"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl2),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Variant #${index + 1}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${variant.totalEffort} pts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = slackLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
            ) {
                variant.tickets.sortedBy { it.id }.forEach { ticket ->
                    TicketChip(ticket)
                }
            }
        }
    }
}

@Composable
private fun TicketChip(ticket: Ticket) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text("${ticket.name} (${ticket.estimation.code()})")
        },
        leadingIcon = {
            EstimationIcon(estimation = ticket.estimation)
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledLeadingIconContentColor = ticket.estimation.color(),
        ),
    )
}

@Composable
private fun EstimationIcon(estimation: Estimation) {
    Icon(
        painter = estimation.icon(),
        contentDescription = null,
        tint = estimation.color(),
    )
}

@Preview
@Composable
private fun CapacityResultPreview() {
    CapacityResultContent(
        state = CapacityResultState(
            teamId = 1,
            team = Team(id = 1, name = "Platform Team", peopleCount = 6, capacity = 26),
            result = estimationCalculatorExample(),
        ),
        onDismiss = {},
        modifier = Modifier.fillMaxSize(),
        onEvent = { },
    )
}

private val variantDisplayComparator =
    compareByDescending<FeasibleTicketVariant> { it.totalEffort }
        .thenByDescending { it.tickets.size }
        .thenBy { it.tickets.minOfOrNull(Ticket::id) ?: 0 }

private const val COLLAPSED_VARIANTS = 8
private const val MAX_VISIBLE_VARIANTS = 30
