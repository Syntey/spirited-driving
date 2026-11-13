package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetSettings
import org.koin.core.annotation.Factory
import com.syntey.spiriteddriving.feature.map.tools.Constants

/**
 * The budget choice the user planned with last; before the first pick, +30 min, with +30 km for the first switch to
 * distance.
 */
@Factory
internal class GetBudgetChoiceUseCase(private val budgetSettings: BudgetSettings) : UseCase<Unit, BudgetChoice> {

    override suspend fun build(args: Unit): BudgetChoice = budgetSettings.get() ?: Constants.Budgets.DEFAULT_CHOICE
}
