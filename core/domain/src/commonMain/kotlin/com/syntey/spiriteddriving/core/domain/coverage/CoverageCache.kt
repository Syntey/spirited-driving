package com.syntey.spiriteddriving.core.domain.coverage

import com.syntey.spiriteddriving.core.domain.settings.CountryCode

/**
 * The last covered area the backend sent, kept on the device, so the map has it at once and offline.
 */
interface CoverageCache {

    /** The kept copy, or `null` when there's none or it can't be read. */
    suspend fun get(): VersionedCoverage?

    /** The vignette countries of the kept copy, without reading its area; `null` when there's none or it can't be read. */
    suspend fun vignetteCountries(): List<CountryCode>?

    /** Keeps [coverage] in place of the previous copy. */
    suspend fun save(coverage: VersionedCoverage)
}
