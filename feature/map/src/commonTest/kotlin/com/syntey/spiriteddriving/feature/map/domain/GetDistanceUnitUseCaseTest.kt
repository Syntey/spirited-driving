package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDistanceUnitUseCaseTest {

    @Test
    fun `a phone in Great Britain reads distances in miles`() = runTest {
        assertEquals(DistanceUnit.Miles, GetDistanceUnitUseCase(FakePlatform("GB")).build(Unit))
    }

    @Test
    fun `a phone in the Czech Republic reads distances in kilometres`() = runTest {
        assertEquals(DistanceUnit.Kilometres, GetDistanceUnitUseCase(FakePlatform("CZ")).build(Unit))
    }
}

private class FakePlatform(override val regionCode: String?) : Platform {
    override val appName = "App"
    override val appVersionName = "1.0"
    override val appBuildNumber = "1"
    override val applicationId = "app.id"
    override val osNameWithVersion = "OS 1"
    override val deviceModel = "Model"
    override val preferredLanguages = listOf("en")
}
