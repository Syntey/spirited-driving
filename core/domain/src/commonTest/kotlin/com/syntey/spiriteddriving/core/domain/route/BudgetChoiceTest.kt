package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class BudgetChoiceTest {

    @Test
    fun `budget is the time budget when the kind is time`() {
        assertEquals(time(30), choice(BudgetKind.Time).budget)
    }

    @Test
    fun `budget is the distance budget when the kind is distance`() {
        assertEquals(distance(30), choice(BudgetKind.Distance).budget)
    }

    @Test
    fun `picking a time budget replaces time and switches to time but keeps distance`() {
        val result = choice(BudgetKind.Distance).with(time(45))

        assertEquals(time(45), result.time)
        assertEquals(distance(30), result.distance)
        assertEquals(BudgetKind.Time, result.kind)
    }

    @Test
    fun `picking a distance budget replaces distance and switches to distance but keeps time`() {
        val result = choice(BudgetKind.Time).with(distance(50))

        assertEquals(distance(50), result.distance)
        assertEquals(time(30), result.time)
        assertEquals(BudgetKind.Distance, result.kind)
    }

    private fun choice(kind: BudgetKind) = BudgetChoice(time = time(30), distance = distance(30), kind = kind)

    private fun time(minutes: Int) = Budget.Time(minutes.minutes)

    private fun distance(kilometres: Int) = Budget.Distance(kilometres.kilometres)
}
