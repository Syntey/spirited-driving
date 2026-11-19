package com.syntey.spiriteddriving.core.domain.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveUnitTest {

    @Test
    fun `a phone in Great Britain reads distances in miles until the user picks`() = runTest {
        val unit = FakeDistanceUnitSettings().observeUnit("GB").first()

        assertEquals(DistanceUnit.Miles, unit)
    }

    @Test
    fun `a phone in the Czech Republic reads distances in kilometres until the user picks`() = runTest {
        val unit = FakeDistanceUnitSettings().observeUnit("CZ").first()

        assertEquals(DistanceUnit.Kilometres, unit)
    }

    @Test
    fun `the unit the user picked wins over the region`() = runTest {
        val unit = FakeDistanceUnitSettings(DistanceUnit.Kilometres).observeUnit("GB").first()

        assertEquals(DistanceUnit.Kilometres, unit)
    }

    @Test
    fun `a later pick emits the picked unit again`() = runTest {
        val settings = FakeDistanceUnitSettings()
        val emitted = mutableListOf<DistanceUnit>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            settings.observeUnit("GB").collect { emitted += it }
        }

        settings.set(DistanceUnit.Kilometres)
        runCurrent()
        settings.set(DistanceUnit.Miles)
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(DistanceUnit.Miles, DistanceUnit.Kilometres, DistanceUnit.Miles), emitted)
    }
}
