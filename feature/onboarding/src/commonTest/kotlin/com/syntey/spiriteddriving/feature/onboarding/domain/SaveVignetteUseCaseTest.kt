package com.syntey.spiriteddriving.feature.onboarding.domain

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveVignetteUseCaseTest {

    @Test
    fun `the answer is saved for Czechia`() = runTest {
        val settings = FakeVignetteSettings()

        SaveVignetteUseCase(settings).build(SaveVignetteUseCase.Args(hasVignette = false))

        assertEquals(mapOf(CountryCode.Czechia to false), settings.answers)
    }

    private class FakeVignetteSettings : VignetteSettings {
        val answers = mutableMapOf<CountryCode, Boolean>()

        override suspend fun hasVignette(country: CountryCode): Boolean? = answers[country]

        override fun observeVignettes(): Flow<Set<CountryCode>> = flowOf(answers.filterValues { it }.keys)

        override suspend fun setHasVignette(country: CountryCode, hasVignette: Boolean) {
            answers[country] = hasVignette
        }
    }
}
