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
)

/**
 * Calculates whether a list of tickets fits into a numeric capacity and proposes alternative subsets that do.
 */
class EstimationCalculator(
    private val estimationWeights: Map<Estimation, Int> = defaultWeights,
) {

    init {
        require(estimationWeights.keys.containsAll(Estimation.entries)) {
            "Every estimation must have a weight"
        }
    }

    /**
     * Evaluates the given tickets against the provided [capacity].
     */
    fun evaluate(
        tickets: List<Ticket>,
        capacity: Int,
    ): EstimationResult {
        require(capacity >= 0) { "Capacity cannot be negative" }

        val orderedTickets = tickets.toList() // enforce determinism regardless of original list implementation
        val totalEffort = orderedTickets.sumOf { effortOf(it.estimation) }

        val feasibleVariants = buildFeasibleVariants(orderedTickets, capacity)

        return EstimationResult(
            canCloseAll = totalEffort <= capacity,
            totalEffort = totalEffort,
            capacity = capacity,
            feasibleVariants = feasibleVariants,
        )
    }

    private fun buildFeasibleVariants(
        tickets: List<Ticket>,
        capacity: Int,
    ): List<FeasibleTicketVariant> {
        if (tickets.isEmpty() || capacity == 0) return emptyList()

        val uniqueVariants = LinkedHashMap<List<Int>, FeasibleTicketVariant>()
        val currentSelection = mutableListOf<Ticket>()

        fun backtrack(startIndex: Int, currentEffort: Int) {
            if (currentEffort > capacity) return

            if (currentSelection.isNotEmpty()) {
                val selectionKey = currentSelection.map { it.id }
                uniqueVariants.putIfAbsent(
                    selectionKey,
                    FeasibleTicketVariant(currentSelection.toList(), currentEffort),
                )
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
        return uniqueVariants.values.toList()
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
