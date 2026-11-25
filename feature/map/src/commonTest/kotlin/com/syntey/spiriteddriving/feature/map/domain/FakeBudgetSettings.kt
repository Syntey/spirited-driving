package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetSettings

internal class FakeBudgetSettings(private var choice: BudgetChoice?) : BudgetSettings {

    override suspend fun get(): BudgetChoice? = choice

    override suspend fun set(choice: BudgetChoice) {
        this.choice = choice
    }
}
