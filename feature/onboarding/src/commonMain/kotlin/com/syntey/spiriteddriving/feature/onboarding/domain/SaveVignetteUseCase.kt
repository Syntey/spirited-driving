package com.syntey.spiriteddriving.feature.onboarding.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import org.koin.core.annotation.Factory

/**
 * Saves whether the user has a Czech motorway vignette, which also completes First launch.
 */
@Factory
internal class SaveVignetteUseCase(private val vignetteSettings: VignetteSettings) : UseCase<SaveVignetteUseCase.Args, Unit> {

    override suspend fun build(args: Args) = vignetteSettings.setHasVignette(CountryCode.Czechia, args.hasVignette)

    data class Args(val hasVignette: Boolean)
}
