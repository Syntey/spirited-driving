package com.syntey.spiriteddriving.core.domain.coverage

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode

/**
 * [CoverageSource] that answers like the backend with [coverage]: `null` when asked with its version.
 * Records the versions it was asked with.
 */
class FakeCoverageSource(private val coverage: VersionedCoverage = VersionedCoverage(Czechia, version = "\"recorded\"")) : CoverageSource {

    val knownVersions: List<String?>
        field = mutableListOf<String?>()

    override suspend fun fetch(knownVersion: String?): VersionedCoverage? {
        knownVersions += knownVersion
        return coverage.takeIf { knownVersion != it.version }
    }

    companion object {
        /** A coarse outline of Czechia, every 24th point of the backend's border, and its vignette countries. */
        val Czechia: Coverage = Coverage(
            area = listOf(
                GeoPolygon(
                    outline = listOf(
                        GeoPoint(50.25032, 12.09287), GeoPoint(49.42437, 12.70886), GeoPoint(48.83319, 13.78977),
                        GeoPoint(48.76268, 14.94773), GeoPoint(48.78645, 15.9851), GeoPoint(48.92529, 17.78158),
                        GeoPoint(49.6815, 18.80479), GeoPoint(49.93967, 18.27953), GeoPoint(50.23545, 17.76609),
                        GeoPoint(50.25753, 16.99803), GeoPoint(50.44722, 16.2029), GeoPoint(50.83956, 15.36656),
                        GeoPoint(50.90513, 14.65259), GeoPoint(50.97831, 14.29426), GeoPoint(50.61243, 13.42189),
                        GeoPoint(50.37045, 12.48707), GeoPoint(50.31576, 12.1247), GeoPoint(50.25032, 12.09287),
                    ),
                ),
            ),
            vignetteCountries = listOf("CZ", "SK", "AT", "HU", "SI").map(::CountryCode),
        )
    }
}
