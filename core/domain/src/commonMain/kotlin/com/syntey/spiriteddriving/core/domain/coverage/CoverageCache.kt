package com.syntey.spiriteddriving.core.domain.coverage

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import kotlinx.coroutines.flow.Flow

/**
 * The last covered area the backend sent, kept on the device, so the map has it at once and offline.
 */
interface CoverageCache {

    /** The kept copy, or `null` when there's none or it can't be read. */
    suspend fun get(): VersionedCoverage?

    /**
     * The vignette countries of the kept copy, now and after each save, without reading its area; `null` while there's
     * none or it can't be read.
     */
    fun observeVignetteCountries(): Flow<List<CountryCode>?>

    /** Keeps [coverage] in place of the previous copy. */
    suspend fun save(coverage: VersionedCoverage)
}
