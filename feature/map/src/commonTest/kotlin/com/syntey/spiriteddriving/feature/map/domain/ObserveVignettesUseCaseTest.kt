package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.FakeVignetteSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveVignettesUseCaseTest {

    @Test
    fun `emits the countries with a vignette now and after each change`() = runTest {
        val settings = FakeVignetteSettings(mapOf(CountryCode.Czechia to true))
        val emitted = mutableListOf<Set<CountryCode>>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveVignettesUseCase(settings).build(Unit).collect { emitted += it }
        }

        settings.setHasVignette(CountryCode.Czechia, false)
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(setOf(CountryCode.Czechia), emptySet()), emitted)
    }
}
