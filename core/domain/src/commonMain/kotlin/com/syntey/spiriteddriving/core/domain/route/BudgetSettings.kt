package com.syntey.spiriteddriving.core.domain.route

/** The budget choice, kept on the device. */
interface BudgetSettings {

    /** The last choice, or `null` before the user picked a budget or when it can't be read. */
    suspend fun get(): BudgetChoice?

    suspend fun set(choice: BudgetChoice)
}
