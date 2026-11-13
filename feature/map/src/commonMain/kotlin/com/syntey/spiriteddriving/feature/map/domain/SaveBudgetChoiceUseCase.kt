package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetSettings
import org.koin.core.annotation.Factory

/**
 * Keeps the budget choice the user picked.
 */
@Factory
internal class SaveBudgetChoiceUseCase(private val budgetSettings: BudgetSettings) : UseCase<BudgetChoice, Unit> {

    override suspend fun build(args: BudgetChoice) = budgetSettings.set(args)
}
