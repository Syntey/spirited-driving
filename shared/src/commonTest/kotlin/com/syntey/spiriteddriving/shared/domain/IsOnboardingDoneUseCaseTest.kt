package com.syntey.spiriteddriving.shared.domain

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.FakeVignetteSettings
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
}
