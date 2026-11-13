package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class BudgetUnitsTest {

    @Test
    fun `30 km moves to the 20 mi chip, the nearest`() {
        assertEquals(Budget.Distance(20.miles), choice(30.kilometres).inUnit(DistanceUnit.Miles).distance)
    }

    @Test
    fun `100 km moves to the 60 mi chip, the nearest`() {
        assertEquals(Budget.Distance(60.miles), choice(100.kilometres).inUnit(DistanceUnit.Miles).distance)
    }

    @Test
    fun `20 mi moves to the 30 km chip, the nearest`() {
        assertEquals(Budget.Distance(30.kilometres), choice(20.miles).inUnit(DistanceUnit.Kilometres).distance)
    }

    @Test
    fun `a stored 5 mi, kept in whole metres, is still the 5 mi chip`() {
        assertEquals(Budget.Distance(5.miles), choice(8046.metres).inUnit(DistanceUnit.Miles).distance)
    }

    @Test
    fun `a distance between two chips moves to the smaller one on a tie`() {
        assertEquals(Budget.Distance(20.kilometres), choice(25.kilometres).inUnit(DistanceUnit.Kilometres).distance)
    }

    @Test
    fun `a distance already on a chip of the unit stays as it is`() {
        val choice = choice(20.miles)

        assertEquals(choice, choice.inUnit(DistanceUnit.Miles))
    }

    @Test
    fun `the time and the kind stay as they are when the distance moves`() {
        val choice = BudgetChoice(
            time = Budget.Time(45.minutes),
            distance = Budget.Distance(30.kilometres),
            kind = BudgetKind.Time,
        )

        val moved = choice.inUnit(DistanceUnit.Miles)

        assertEquals(Budget.Time(45.minutes), moved.time)
        assertEquals(BudgetKind.Time, moved.kind)
    }

    private fun choice(distance: Length) = BudgetChoice(
        time = Budget.Time(30.minutes),
        distance = Budget.Distance(distance),
        kind = BudgetKind.Distance,
    )
}
