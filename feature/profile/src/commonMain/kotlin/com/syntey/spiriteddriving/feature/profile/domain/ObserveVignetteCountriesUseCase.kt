package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.feature.profile.model.VignetteCountry
import com.syntey.spiriteddriving.feature.profile.tools.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Emits the countries with a motorway vignette to ask about, now and after each change to them or to the covered area:
 * the covered area's, or Czechia until the covered area has loaded. Each is named in English by the OS and says whether
 * the user has a vignette.
 */
@Factory
internal class ObserveVignetteCountriesUseCase(
    private val coverageCache: CoverageCache,
    private val vignetteSettings: VignetteSettings,
    @Provided private val platform: Platform,
) : FlowUseCase<Unit, List<VignetteCountry>> {

    override fun build(args: Unit): Flow<List<VignetteCountry>> =
        combine(coverageCache.observeVignetteCountries(), vignetteSettings.observeVignettes()) { kept, vignettes ->
            val countries = kept.orEmpty().ifEmpty { Constants.Vignettes.BUILT_IN }
            countries.map { code ->
                VignetteCountry(
                    code = code,
                    name = platform.countryName(code.value) ?: code.value,
                    hasVignette = code in vignettes,
                )
            }
        }
}
