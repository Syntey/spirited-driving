package com.syntey.spiriteddriving.data.persistence.persistence.coverage

import com.syntey.spiriteddriving.core.domain.coverage.Coverage
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toStored
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The covered area as kept on the device. Its shape is the stored format, so renaming a field loses the kept copy
 * (it's then fetched again).
 */
@Serializable
internal data class StoredCoverage(
    @SerialName("area") val area: List<StoredPolygon>,
    @SerialName("vignetteCountries") val vignetteCountries: List<String>,
    @SerialName("version") val version: String?,
)

@Serializable
internal data class StoredPolygon(
    @SerialName("outline") val outline: List<StoredPoint>,
    @SerialName("holes") val holes: List<List<StoredPoint>>,
)

internal fun VersionedCoverage.toStored() = StoredCoverage(
    area = coverage.area.map { polygon ->
        StoredPolygon(outline = polygon.outline.map { it.toStored() }, holes = polygon.holes.map { hole -> hole.map { it.toStored() } })
    },
    vignetteCountries = coverage.vignetteCountries.map { it.value },
    version = version,
)

internal fun StoredCoverage.toDomain() = VersionedCoverage(
    coverage = Coverage(
        area = area.map { polygon ->
            GeoPolygon(outline = polygon.outline.map { it.toDomain() }, holes = polygon.holes.map { hole -> hole.map { it.toDomain() } })
        },
        vignetteCountries = vignetteCountries.map(::CountryCode),
    ),
    version = version,
)
