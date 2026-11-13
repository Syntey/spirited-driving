package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/** Emits the countries the user has a motorway vignette for, now and after each change. */
@Factory
internal class ObserveVignettesUseCase(private val vignetteSettings: VignetteSettings) : FlowUseCase<Unit, Set<CountryCode>> {

    override fun build(args: Unit): Flow<Set<CountryCode>> = vignetteSettings.observeVignettes()
}
