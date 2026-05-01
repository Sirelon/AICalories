package com.sirelon.aicalories.features.agile.capacity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.sirelon.aicalories.designsystem.AppScaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppChip
import com.sirelon.aicalories.designsystem.AppChipDefaults
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppSectionHeader
import com.sirelon.aicalories.designsystem.AppTheme
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
        modifier = modifier.shadow(elevation = AppDimens.Size.m),
        color = AppTheme.colors.surface,
    ) {
        AppScaffold(
            modifier = Modifier.fillMaxSize(),
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
                        title = stringResource(Res.string.capacity_result_title),
                        subtitle = state.team?.name ?: stringResource(Res.string.estimation_outcome),
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
                            text = stringResource(Res.string.crunching_numbers),
                            style = AppTheme.typography.body,
                            modifier = Modifier.padding(top = AppDimens.Spacing.m),
                        )
                        TextButton(onClick = { onEvent(CapacityResultEvent.Refresh) }) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues),
                    contentPadding = PaddingValues(
                        start = AppDimens.Spacing.xl3,
                        end = AppDimens.Spacing.xl3,
                        top = paddingValues.calculateTopPadding() + AppDimens.Spacing.xl3,
                        bottom = paddingValues.calculateBottomPadding() + AppDimens.Spacing.xl3,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
                ) {
                    item {
                        AppSectionHeader(
                            title = stringResource(Res.string.capacity_result_title),
                            subtitle = state.team?.name ?: stringResource(Res.string.estimation_outcome),
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
        AppTheme.colors.primary
    } else {
        AppTheme.colors.error
    }
    val scenarioChipColors = AppChipDefaults.capacityColors(fitsPessimistic, fitsOptimistic)
    val metadataChipColors = AppChipDefaults.neutralColors()
    val statusText = when {
        fitsPessimistic -> stringResource(Res.string.status_everything_fits)
        fitsOptimistic -> stringResource(Res.string.status_scope_fits_optimistic)
        else -> stringResource(Res.string.status_need_more_points, abs(optimisticRemaining))
    }
    val detailText = when {
        fitsPessimistic -> stringResource(Res.string.detail_effort_vs_capacity, result.totalEffort, capacity.pessimistic, capacity.optimistic, riskPercent)
        fitsOptimistic -> stringResource(Res.string.detail_effort_exceeds_pessimistic, result.totalEffort, abs(pessimisticRemaining), capacity.optimistic)
        else -> stringResource(Res.string.detail_effort_above_optimistic, result.totalEffort, capacity.optimistic)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = AppDimens.BorderWidth.s,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            Text(
                text = statusText,
                style = AppTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                color = statusColor,
            )
            Text(
                text = detailText,
                style = AppTheme.typography.body,
                color = AppTheme.colors.onSurfaceMuted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
            ) {
                AppChip(
                    text = stringResource(Res.string.capacity_format, capacity.pessimistic, capacity.optimistic),
                    icon = Icons.Outlined.Assessment,
                    colors = scenarioChipColors,
                )
                AppChip(
                    text = stringResource(Res.string.base_capacity, capacity.base),
                    icon = Icons.Outlined.Tune,
                    colors = metadataChipColors,
                )
                AppChip(
                    text = stringResource(Res.string.risk_format, riskPercent),
                    icon = Icons.Outlined.Warning,
                    colors = AppChipDefaults.errorColors(),
                )
                AppChip(
                    text = stringResource(Res.string.total_effort, result.totalEffort),
                    icon = Icons.Outlined.TrendingUp,
                    colors = scenarioChipColors,
                )
                val pessimisticBalance = if (pessimisticRemaining < 0) {
                    stringResource(Res.string.pessimistic_over, abs(pessimisticRemaining))
                } else {
                    stringResource(Res.string.pessimistic_slack, pessimisticRemaining)
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
                    stringResource(Res.string.optimistic_over, abs(optimisticRemaining))
                } else {
                    stringResource(Res.string.optimistic_slack, optimisticRemaining)
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
                        text = stringResource(Res.string.variants_count, result.totalVariants),
                        icon = Icons.Outlined.GridView,
                        colors = metadataChipColors,
                    )
                }
                team?.peopleCount?.takeIf { it > 0 }?.let { people ->
                    AppChip(
                        text = stringResource(Res.string.people_count, people),
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
                text = stringResource(Res.string.feasible_ticket_variants),
                style = AppTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(Res.string.sorted_by_fit),
                style = AppTheme.typography.body,
                color = AppTheme.colors.onSurfaceMuted,
            )
        }

        if (result.canCloseAll) {
            Text(
                text = stringResource(Res.string.all_tickets_fit),
                style = AppTheme.typography.body,
                color = AppTheme.colors.onSurfaceMuted,
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
                    stringResource(Res.string.add_tickets_hint)
                } else {
                    stringResource(Res.string.no_tickets_fit)
                },
                style = AppTheme.typography.body,
                color = AppTheme.colors.error,
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
                        stringResource(Res.string.showing_best_capped, variantsToDisplay.size, totalVariants, MAX_VISIBLE_VARIANTS)
                    } else if (showAll || !hasHidden) {
                        stringResource(Res.string.showing_best, variantsToDisplay.size, totalVariants)
                    } else {
                        stringResource(Res.string.showing_top, COLLAPSED_VARIANTS, totalVariants)
                    },
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.onSurfaceMuted,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) stringResource(Res.string.collapse) else stringResource(Res.string.show_more))
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
        slack == 0 -> stringResource(Res.string.exact_fit)
        slack > 0 -> stringResource(Res.string.slack_free, slack)
        else -> stringResource(Res.string.over_by, abs(slack))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = AppDimens.BorderWidth.s,
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
                    text = stringResource(Res.string.variant_format, index + 1),
                    style = AppTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.effort_points, variant.totalEffort),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.primary,
                )
            }
            Text(
                text = slackLabel,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onSurfaceMuted,
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
        text = stringResource(Res.string.ticket_format, ticket.name, ticket.estimation.code()),
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
