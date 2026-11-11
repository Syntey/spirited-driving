package com.syntey.spiriteddriving.shared.domain

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsOnboardingDoneUseCaseTest {

    @Test
    fun `first launch is not done while the vignette is unanswered`() = runTest {
        assertFalse(IsOnboardingDoneUseCase(FakeVignetteSettings()).build(Unit))
    }

    @Test
    fun `answering yes completes first launch`() = runTest {
        val settings = FakeVignetteSettings().apply { setHasVignette(CountryCode.Czechia, hasVignette = true) }

        assertTrue(IsOnboardingDoneUseCase(settings).build(Unit))
    }

    @Test
    fun `answering no completes first launch too`() = runTest {
        val settings = FakeVignetteSettings().apply { setHasVignette(CountryCode.Czechia, hasVignette = false) }

        assertTrue(IsOnboardingDoneUseCase(settings).build(Unit))
    }

    @Test
    fun `a vignette for another country does not complete first launch`() = runTest {
        val settings = FakeVignetteSettings().apply { setHasVignette(CountryCode("AT"), hasVignette = true) }

        assertFalse(IsOnboardingDoneUseCase(settings).build(Unit))
    }

    private class FakeVignetteSettings : VignetteSettings {
        private val answers = mutableMapOf<CountryCode, Boolean>()

        override suspend fun hasVignette(country: CountryCode): Boolean? = answers[country]

        override suspend fun setHasVignette(country: CountryCode, hasVignette: Boolean) {
            answers[country] = hasVignette
        }
    }
}
