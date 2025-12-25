package com.sirelon.aicalories.features.agile.capacity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppChip
import com.sirelon.aicalories.designsystem.AppChipDefaults
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppSectionHeader
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
import kotlin.math.roundToInt

@Composable
internal fun CapacityResultScreen(
    teamId: Int,
) {
    val viewModel: CapacityResultViewModel = koinViewModel(
        key = "capacity_result_$teamId",
        parameters = { parametersOf(teamId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    CapacityResultContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun CapacityResultContent(
    state: CapacityResultState,
    onEvent: (CapacityResultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.result

    Surface(
        modifier = modifier.shadow(elevation = 8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
            ),
        ) { paddingValues ->
            if (result == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing.xl3, vertical = AppDimens.Spacing.xl3),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
                ) {
                    AppSectionHeader(
                        title = "Capacity result",
                        subtitle = state.team?.name ?: "Estimation outcome",
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
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
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing.xl3, vertical = AppDimens.Spacing.xl3),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
                ) {
                    item {
                        AppSectionHeader(
                            title = "Capacity result",
                            subtitle = state.team?.name ?: "Estimation outcome",
                        )
                    }
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
    val capacity = result.capacity
    val riskPercent = (capacity.riskFactor * 100).roundToInt()
    val pessimisticRemaining = capacity.pessimistic - result.totalEffort
    val optimisticRemaining = capacity.optimistic - result.totalEffort
    val fitsPessimistic = pessimisticRemaining >= 0
    val fitsOptimistic = optimisticRemaining >= 0
    val statusColor = if (fitsOptimistic) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val scenarioChipColors = AppChipDefaults.capacityColors(fitsPessimistic, fitsOptimistic)
    val metadataChipColors = AppChipDefaults.neutralColors()
    val statusText = when {
        fitsPessimistic -> "Everything fits even with the pessimistic capacity."
        fitsOptimistic -> "Scope fits only in the optimistic scenario."
        else -> "You need ${abs(optimisticRemaining)} more points."
    }
    val detailText = when {
        fitsPessimistic -> "Effort ${result.totalEffort} vs capacity ${capacity.pessimistic}-${capacity.optimistic} (risk ±$riskPercent%)."
        fitsOptimistic -> "Effort ${result.totalEffort} exceeds pessimistic capacity by ${abs(pessimisticRemaining)} but fits optimistic ${capacity.optimistic}."
        else -> "Effort ${result.totalEffort} is above the optimistic capacity ${capacity.optimistic}."
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
                AppChip(
                    text = "Capacity ${capacity.pessimistic}-${capacity.optimistic}",
                    icon = Icons.Outlined.Assessment,
                    colors = scenarioChipColors,
                )
                AppChip(
                    text = "Base ${capacity.base}",
                    icon = Icons.Outlined.Tune,
                    colors = metadataChipColors,
                )
                AppChip(
                    text = "Risk ±$riskPercent%",
                    icon = Icons.Outlined.Warning,
                    colors = AppChipDefaults.errorColors(),
                )
                AppChip(
                    text = "Effort ${result.totalEffort}",
                    icon = Icons.Outlined.TrendingUp,
                    colors = scenarioChipColors,
                )
                val pessimisticBalance = if (pessimisticRemaining < 0) {
                    "Pessimistic over ${abs(pessimisticRemaining)}"
                } else {
                    "Pessimistic slack $pessimisticRemaining"
                }
                AppChip(
                    text = pessimisticBalance,
                    icon = if (pessimisticRemaining < 0) {
                        Icons.Outlined.TrendingDown
                    } else {
                        Icons.Outlined.TrendingUp
                    },
                    colors = if (pessimisticRemaining < 0) {
                        AppChipDefaults.errorColors()
                    } else {
                        AppChipDefaults.successColors()
                    },
                )
                val optimisticBalance = if (optimisticRemaining < 0) {
                    "Optimistic over ${abs(optimisticRemaining)}"
                } else {
                    "Optimistic slack $optimisticRemaining"
                }
                AppChip(
                    text = optimisticBalance,
                    icon = if (optimisticRemaining < 0) {
                        Icons.Outlined.TrendingDown
                    } else {
                        Icons.Outlined.TrendingUp
                    },
                    colors = if (optimisticRemaining < 0) {
                        AppChipDefaults.errorColors()
                    } else {
                        AppChipDefaults.successColors()
                    },
                )
                if (result.totalVariants > 0) {
                    AppChip(
                        text = "${result.totalVariants} variants",
                        icon = Icons.Outlined.GridView,
                        colors = metadataChipColors,
                    )
                }
                team?.peopleCount?.takeIf { it > 0 }?.let { people ->
                    AppChip(
                        text = "$people people",
                        icon = Icons.Outlined.PeopleOutline,
                        colors = metadataChipColors,
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
    val capacityForVariants = result.capacity.pessimistic

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
                text = "Sorted by best fit to the pessimistic capacity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (result.canCloseAll) {
            Text(
                text = "All tickets fit into the optimistic capacity, so no alternative subsets are needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        val sortedVariants = remember(result) {
            result.feasibleVariants.sortedWith(variantDisplayComparator)
        }
        val totalVariants = result.totalVariants.takeIf { it > 0 } ?: sortedVariants.size
        var showAll by rememberSaveable { mutableStateOf(false) }

        val maxVisible = if (showAll) MAX_VISIBLE_VARIANTS else COLLAPSED_VARIANTS
        val variantsToDisplay = sortedVariants.take(maxVisible)
        val hasHidden = totalVariants > variantsToDisplay.size
        val isCapped = totalVariants > MAX_VISIBLE_VARIANTS

        if (totalVariants == 0 || sortedVariants.isEmpty()) {
            Text(
                text = if (result.totalEffort == 0) {
                    "Add tickets to see how they stack against the current risk-adjusted capacity."
                } else {
                    "No combination of tickets fits the current pessimistic capacity. Trim scope or increase capacity."
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
                capacity = capacityForVariants,
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
    AppChip(
        text = "${ticket.name} (${ticket.estimation.code()})",
        leadingIcon = {
            EstimationIcon(estimation = ticket.estimation)
        },
        colors = AppChipDefaults.accentColors(ticket.estimation.color()),
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
