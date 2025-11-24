package com.sirelon.aicalories.features.agile

import com.sirelon.aicalories.features.agile.model.Ticket

/**
 * A computed subset of [tickets] that fits within the requested capacity together with its total effort.
 */
data class FeasibleTicketVariant(
    val tickets: List<Ticket>,
    val totalEffort: Int,
)

/**
 * Result returned by [EstimationCalculator.evaluate].
 */
data class EstimationResult(
    val canCloseAll: Boolean,
    val totalEffort: Int,
    val capacity: Int,
    val feasibleVariants: List<FeasibleTicketVariant>,
    val totalVariants: Int = feasibleVariants.size,
)

/**
 * Calculates whether a list of tickets fits into a numeric capacity and proposes alternative subsets that do.
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
     * Evaluates the given tickets against the provided [capacity].
     */
    fun evaluate(
        tickets: List<Ticket>,
        capacity: Int,
    ): EstimationResult {
        require(capacity >= 0) { "Capacity cannot be negative" }

        val orderedTickets =
            tickets.toList() // enforce determinism regardless of original list implementation
        val totalEffort = orderedTickets.sumOf { effortOf(it.estimation) }

        val (feasibleVariants, totalVariants) = buildFeasibleVariants(orderedTickets, capacity)

        return EstimationResult(
            canCloseAll = totalEffort <= capacity,
            totalEffort = totalEffort,
            capacity = capacity,
            feasibleVariants = feasibleVariants,
            totalVariants = totalVariants,
        )
    }

    private fun buildFeasibleVariants(
        tickets: List<Ticket>,
        capacity: Int,
    ): Pair<List<FeasibleTicketVariant>, Int> {
        if (tickets.isEmpty() || capacity == 0) return emptyList<FeasibleTicketVariant>() to 0

        val storedVariants = mutableListOf<FeasibleTicketVariant>()
        var totalVariants = 0
        val currentSelection = mutableListOf<Ticket>()

        fun backtrack(startIndex: Int, currentEffort: Int) {
            if (currentEffort > capacity) return

            if (currentSelection.isNotEmpty()) {
                val variant = FeasibleTicketVariant(
                    tickets = currentSelection.toList(),
                    totalEffort = currentEffort,
                )
                totalVariants += 1
                if (storedVariants.size < maxVariantsToKeep) {
                    storedVariants.add(variant)
                } else {
                    val worstVariant = storedVariants.minWithOrNull(variantComparator)
                    if (worstVariant != null && variantComparator.compare(variant, worstVariant) > 0) {
                        val replaceIndex = storedVariants.indexOf(worstVariant)
                        if (replaceIndex >= 0) {
                            storedVariants[replaceIndex] = variant
                        }
                    }
                }
            }

            for (index in startIndex until tickets.size) {
                val ticket = tickets[index]
                val nextEffort = currentEffort + effortOf(ticket.estimation)
                if (nextEffort > capacity) continue

                currentSelection.add(ticket)
                backtrack(index + 1, nextEffort)
                currentSelection.removeAt(currentSelection.lastIndex)
            }
        }

        backtrack(startIndex = 0, currentEffort = 0)
        val rankedVariants = storedVariants.sortedWith(variantComparator)
        return rankedVariants to totalVariants
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
 * and a capacity equal to `2 × XL` (two extra-large tickets worth of effort).
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

    return EstimationCalculator().evaluate(tickets, capacity)
}
