package com.syntey.spiriteddriving.shared.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import org.koin.core.annotation.Factory

/**
 * First launch is done once the vignette question is answered. Until then it starts again from the beginning.
 */
@Factory
internal class IsOnboardingDoneUseCase(private val vignetteSettings: VignetteSettings) : UseCase<Unit, Boolean> {
    override suspend fun build(args: Unit): Boolean = vignetteSettings.hasVignette(CountryCode.Czechia) != null
}
