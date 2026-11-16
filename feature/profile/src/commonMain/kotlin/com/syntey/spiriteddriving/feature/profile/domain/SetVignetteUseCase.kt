package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import org.koin.core.annotation.Factory

/** Keeps whether the user has a motorway vignette for a country. */
@Factory
internal class SetVignetteUseCase(private val vignetteSettings: VignetteSettings) : UseCase<SetVignetteUseCase.Args, Unit> {

    data class Args(val country: CountryCode, val hasVignette: Boolean)

    override suspend fun build(args: Args) = vignetteSettings.setHasVignette(args.country, args.hasVignette)
}
