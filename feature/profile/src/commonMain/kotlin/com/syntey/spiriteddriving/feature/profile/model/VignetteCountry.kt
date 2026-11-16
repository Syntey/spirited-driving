package com.syntey.spiriteddriving.feature.profile.model

import com.syntey.spiriteddriving.core.domain.settings.CountryCode

/** A country a motorway vignette is asked about, named as the phone names it. */
data class VignetteCountry(val code: CountryCode, val name: String, val hasVignette: Boolean)
