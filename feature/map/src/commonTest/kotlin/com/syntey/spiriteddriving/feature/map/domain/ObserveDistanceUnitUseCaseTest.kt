package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.domain.settings.FakeDistanceUnitSettings
import com.syntey.spiriteddriving.core.platform.binding.Platform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveDistanceUnitUseCaseTest {

    @Test
    fun `a phone in Great Britain reads distances in miles until the user picks`() = runTest {
        val useCase = ObserveDistanceUnitUseCase(FakeDistanceUnitSettings(), FakePlatform("GB"))

        assertEquals(DistanceUnit.Miles, useCase.build(Unit).first())
    }

    @Test
    fun `a phone in the Czech Republic reads distances in kilometres until the user picks`() = runTest {
        val useCase = ObserveDistanceUnitUseCase(FakeDistanceUnitSettings(), FakePlatform("CZ"))

        assertEquals(DistanceUnit.Kilometres, useCase.build(Unit).first())
    }

    @Test
    fun `the unit the user picked wins over the region`() = runTest {
        val useCase = ObserveDistanceUnitUseCase(FakeDistanceUnitSettings(DistanceUnit.Kilometres), FakePlatform("GB"))

        assertEquals(DistanceUnit.Kilometres, useCase.build(Unit).first())
    }

    @Test
    fun `a later pick emits the picked unit again`() = runTest {
        val settings = FakeDistanceUnitSettings()
        val emitted = mutableListOf<DistanceUnit>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveDistanceUnitUseCase(settings, FakePlatform("GB")).build(Unit).collect { emitted += it }
        }

        settings.set(DistanceUnit.Kilometres)
        runCurrent()
        settings.set(DistanceUnit.Miles)
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(DistanceUnit.Miles, DistanceUnit.Kilometres, DistanceUnit.Miles), emitted)
    }
}

private class FakePlatform(override val regionCode: String?) : Platform {
    override val appName = "App"
    override val appVersionName = "1.0"
    override val appBuildNumber = "1"
    override val applicationId = "app.id"
    override val storeReviewUrl = "https://store.test/app"
    override val osNameWithVersion = "OS 1"
    override val deviceModel = "Model"
    override val preferredLanguages = listOf("en")

    override fun countryName(code: String): String? = null

    override fun openSourceLibrariesJson(): String = ""
}
