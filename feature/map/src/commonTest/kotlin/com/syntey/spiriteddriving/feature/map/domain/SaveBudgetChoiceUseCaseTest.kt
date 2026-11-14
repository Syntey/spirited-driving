package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.BudgetSettings
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class SaveBudgetChoiceUseCaseTest {

    @Test
    fun `saves the choice`() = runTest {
        val settings = FakeBudgetSettings()
        val choice = BudgetChoice(
            time = Budget.Time(45.minutes),
            distance = Budget.Distance(50.kilometres),
            kind = BudgetKind.Time,
        )

        SaveBudgetChoiceUseCase(settings).build(choice)

        assertEquals(choice, settings.saved)
    }

    private class FakeBudgetSettings : BudgetSettings {

        var saved: BudgetChoice? = null
            private set

        override suspend fun get(): BudgetChoice? = saved

        override suspend fun set(choice: BudgetChoice) {
            saved = choice
        }
    }
}
