package com.syntey.spiriteddriving.feature.profile.tools

import com.syntey.spiriteddriving.core.domain.settings.CountryCode

internal object Constants {

    object Vignettes {
        // Asked about until the covered area has loaded, which lists the countries the app covers
        val BUILT_IN: List<CountryCode> = listOf(CountryCode.Czechia)
    }
}
