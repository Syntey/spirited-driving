package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.BudgetSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import com.syntey.spiriteddriving.feature.map.tools.Constants

class GetBudgetChoiceUseCaseTest {

    @Test
    fun `returns the stored choice`() = runTest {
        val stored = BudgetChoice(
            time = Budget.Time(45.minutes),
            distance = Budget.Distance(50.kilometres),
            kind = BudgetKind.Distance,
        )

        assertEquals(stored, GetBudgetChoiceUseCase(FakeBudgetSettings(stored)).build(Unit))
    }

    @Test
    fun `returns the default when nothing is stored`() = runTest {
        assertEquals(Constants.Budgets.DEFAULT_CHOICE, GetBudgetChoiceUseCase(FakeBudgetSettings(null)).build(Unit))
    }

    private class FakeBudgetSettings(private var choice: BudgetChoice?) : BudgetSettings {

        override suspend fun get(): BudgetChoice? = choice

        override suspend fun set(choice: BudgetChoice) {
            this.choice = choice
        }
    }
}
