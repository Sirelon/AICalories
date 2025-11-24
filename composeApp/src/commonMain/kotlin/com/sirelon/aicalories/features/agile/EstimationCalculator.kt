package com.sirelon.aicalories.features.agile

import com.sirelon.aicalories.features.agile.model.Ticket
import kotlin.math.roundToInt

/**
 * A computed subset of [tickets] that fits within the requested capacity together with its total effort.
 */
data class FeasibleTicketVariant(
    val tickets: List<Ticket>,
    val totalEffort: Int,
)

/**
 * Risk-adjusted capacity window derived from a team's base capacity.
 */
data class CapacityRange(
    val base: Int,
    val riskFactor: Double,
    val pessimistic: Int,
    val optimistic: Int,
)

/**
 * Result returned by [EstimationCalculator.evaluate].
 */
data class EstimationResult(
    val canCloseAll: Boolean,
    val totalEffort: Int,
    val capacity: CapacityRange,
    val feasibleVariants: List<FeasibleTicketVariant>,
    val totalVariants: Int = feasibleVariants.size,
)

/**
 * Calculates whether a list of tickets fits into a numeric capacity and proposes alternative subsets that do.
 *
 * The calculator is intentionally defensive:
 * - risk factors are clamped to `[0, 1]` and non-finite inputs fall back to `0`.
 * - feasible variants are only generated when they are needed, and only the best variants (by effort/size/id)
 *   are retained to avoid memory blow-ups.
 */
class EstimationCalculator() {

    private val estimationWeights: Map<Estimation, Int> = defaultWeights
    private val maxVariantsToKeep: Int = DEFAULT_MAX_VARIANTS

    init {
        require(maxVariantsToKeep > 0) { "maxVariantsToKeep must be positive" }
    }

    private val variantComparator: Comparator<FeasibleTicketVariant> =
        compareByDescending<FeasibleTicketVariant> { it.totalEffort }
            .thenByDescending { it.tickets.size }
            .thenBy { it.tickets.minOfOrNull(Ticket::id) ?: 0 }

    /**
     * Evaluates the given tickets against the provided [capacity], expanding it into a
     * pessimistic/optimistic range using [riskFactor].
     */
    fun evaluate(
        tickets: List<Ticket>,
        capacity: Int,
        riskFactor: Double = 0.0,
    ): EstimationResult {
        require(capacity >= 0) { "Capacity cannot be negative" }

        val normalizedRisk = normalizeRiskFactor(riskFactor)
        val orderedTickets = tickets.toList() // deterministic regardless of input list type
        val ticketEfforts = orderedTickets.map { effortOf(it.estimation) }
        val totalEffort = ticketEfforts.sum()
        val capacityRange = buildCapacityRange(capacity, normalizedRisk)

        val canCloseAll = totalEffort <= capacityRange.optimistic
        val (feasibleVariants, totalVariants) = if (canCloseAll || capacityRange.pessimistic == 0) {
            emptyList<FeasibleTicketVariant>() to 0
        } else {
            buildFeasibleVariants(
                tickets = orderedTickets,
                efforts = ticketEfforts,
                capacity = capacityRange.pessimistic,
            )
        }

        return EstimationResult(
            canCloseAll = canCloseAll,
            totalEffort = totalEffort,
            capacity = capacityRange,
            feasibleVariants = feasibleVariants,
            totalVariants = totalVariants,
        )
    }

    private fun buildFeasibleVariants(
        tickets: List<Ticket>,
        efforts: List<Int>,
        capacity: Int,
    ): Pair<List<FeasibleTicketVariant>, Int> {
        if (tickets.isEmpty() || capacity <= 0) return emptyList<FeasibleTicketVariant>() to 0

        val storedVariants = mutableListOf<FeasibleTicketVariant>()
        var totalVariants = 0
        val selectionIndices = mutableListOf<Int>()

        fun considerVariant(currentEffort: Int) {
            if (selectionIndices.isEmpty()) return
            val variantTickets = selectionIndices.map(tickets::get)
            val variant = FeasibleTicketVariant(
                tickets = variantTickets,
                totalEffort = currentEffort,
            )
            totalVariants += 1
            if (storedVariants.size < maxVariantsToKeep) {
                storedVariants.add(variant)
                return
            }

            val worstVariant = storedVariants.minWithOrNull(variantComparator) ?: return
            if (variantComparator.compare(variant, worstVariant) > 0) {
                val replaceIndex = storedVariants.indexOf(worstVariant)
                if (replaceIndex >= 0) storedVariants[replaceIndex] = variant
            }
        }

        fun backtrack(startIndex: Int, currentEffort: Int) {
            if (currentEffort > capacity) return
            considerVariant(currentEffort)

            for (index in startIndex until tickets.size) {
                val nextEffort = currentEffort + efforts[index]
                if (nextEffort > capacity) continue

                selectionIndices.add(index)
                backtrack(index + 1, nextEffort)
                selectionIndices.removeAt(selectionIndices.lastIndex)
            }
        }

        backtrack(startIndex = 0, currentEffort = 0)
        val rankedVariants = storedVariants.sortedWith(variantComparator)
        return rankedVariants to totalVariants
    }

    private fun buildCapacityRange(
        capacity: Int,
        riskFactor: Double,
    ): CapacityRange {
        val adjustment = capacity * riskFactor
        val pessimistic = (capacity - adjustment).roundToInt().coerceAtLeast(0)
        val optimistic = (capacity + adjustment).roundToInt()
        return CapacityRange(
            base = capacity,
            riskFactor = riskFactor,
            pessimistic = pessimistic,
            optimistic = optimistic,
        )
    }

    private fun normalizeRiskFactor(riskFactor: Double): Double {
        if (!riskFactor.isFinite()) return 0.0
        return riskFactor.coerceIn(0.0, 1.0)
    }

    private fun effortOf(estimation: Estimation): Int =
        estimationWeights[estimation]
            ?: error("No weight provided for $estimation")

    companion object {
        /**
         * Fibonacci-inspired mapping commonly used for story points.
         */
        val defaultWeights: Map<Estimation, Int> = mapOf(
            Estimation.XS to 1,
            Estimation.S to 2,
            Estimation.M to 3,
            Estimation.L to 5,
            Estimation.XL to 8,
        )

        private const val DEFAULT_MAX_VARIANTS = 120
    }
}

/**
 * Demonstrates using [EstimationCalculator] with the requested ticket set `[S, XL, S, XS, L]`
 * and a capacity equal to `2 × XL` (two extra-large tickets worth of effort) with a 20% risk
 * factor.
 */
fun estimationCalculatorExample(): EstimationResult {
    val tickets = listOf(
        Ticket(id = 101, name = "Login polish", estimation = Estimation.S),
        Ticket(id = 102, name = "Billing revamp", estimation = Estimation.XL),
        Ticket(id = 103, name = "Docs update", estimation = Estimation.S),
        Ticket(id = 104, name = "Copy tweak", estimation = Estimation.XS),
        Ticket(id = 105, name = "Profile clean-up", estimation = Estimation.L),
    )

    val capacity = 2 * EstimationCalculator.defaultWeights.getValue(Estimation.XL)
    val riskFactor = 0.2

    return EstimationCalculator().evaluate(tickets, capacity, riskFactor)
}
