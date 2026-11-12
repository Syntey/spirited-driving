package com.syntey.spiriteddriving.core.domain.coverage

/**
 * The covered area, from the backend.
 */
interface CoverageSource {

    /**
     * The backend's covered area, or `null` when it's still the one with [knownVersion], so the copy the app has is current.
     */
    suspend fun fetch(knownVersion: String?): VersionedCoverage?
}

/**
 * A covered area and the backend's version of it, which tells later whether it changed.
 */
data class VersionedCoverage(val coverage: Coverage, val version: String?)
