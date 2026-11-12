package com.syntey.spiriteddriving.core.domain.coverage

/**
 * The last covered area the backend sent, kept on the device, so the map has it at once and offline.
 */
interface CoverageCache {

    /** The kept copy, or `null` when there's none or it can't be read. */
    suspend fun get(): VersionedCoverage?

    /** Keeps [coverage] in place of the previous copy. */
    suspend fun save(coverage: VersionedCoverage)
}
