package com.syntey.spiriteddriving.core.domain.route

/** Which unit a budget is in. */
enum class BudgetKind { Time, Distance }

/**
 * The budget the user plans with, in [kind], and the value picked last in each unit, so switching back to a unit
 * returns to it.
 */
data class BudgetChoice(val time: Budget.Time, val distance: Budget.Distance, val kind: BudgetKind) {

    /** The budget in [kind]. */
    val budget: Budget
        get() = when (kind) {
            BudgetKind.Time -> time
            BudgetKind.Distance -> distance
        }

    /** [budget] picked: it becomes the value in its unit, and its unit the one in use. */
    fun with(budget: Budget): BudgetChoice = when (budget) {
        is Budget.Time -> copy(time = budget, kind = BudgetKind.Time)
        is Budget.Distance -> copy(distance = budget, kind = BudgetKind.Distance)
    }
}
