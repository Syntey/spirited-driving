package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class BudgetTextTest {

    @Test
    fun `under an hour shows minutes`() {
        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.budget_minutes, 45),
            budgetText(Budget.Time(45.minutes), DistanceUnit.Kilometres),
        )
    }

    @Test
    fun `a whole hour shows hours without decimals`() {
        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.budget_hours, "1"),
            budgetText(Budget.Time(60.minutes), DistanceUnit.Kilometres),
        )
    }

    @Test
    fun `an hour and a half shows one decimal`() {
        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.budget_hours, "1.5"),
            budgetText(Budget.Time(90.minutes), DistanceUnit.Kilometres),
        )
    }

    @Test
    fun `two hours shows hours without decimals`() {
        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.budget_hours, "2"),
            budgetText(Budget.Time(120.minutes), DistanceUnit.Kilometres),
        )
    }

    @Test
    fun `kilometres show whole kilometres`() {
        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.budget_kilometres, 50),
            budgetText(Budget.Distance(50.kilometres), DistanceUnit.Kilometres),
        )
    }

    @Test
    fun `miles show whole miles`() {
        assertEquals(StringDesc.ResourceFormatted(MR.strings.budget_miles, 20), budgetText(Budget.Distance(20.miles), DistanceUnit.Miles))
    }

    @Test
    fun `a distance rounds to whole miles`() {
        assertEquals(StringDesc.ResourceFormatted(MR.strings.budget_miles, 5), budgetText(Budget.Distance(8046.metres), DistanceUnit.Miles))
    }
}
