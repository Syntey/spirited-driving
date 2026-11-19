package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.Platform

/** [Platform] for a phone in Czechia, with nothing to read but the region. */
internal class FakePlatform(override val regionCode: String? = "CZ") : Platform {
    override val appName = "Spirited Driving"
    override val appVersionName = "1.0.0"
    override val appBuildNumber = "1"
    override val applicationId = "com.syntey.spiriteddriving"
    override val osNameWithVersion = "Test 1"
    override val deviceModel = "Test device"
    override val preferredLanguages = listOf("en")
    override val storeReviewUrl = ""

    override fun countryName(code: String): String? = null

    override fun openSourceLibrariesJson(): String = "[]"
}
