package com.syntey.spiriteddriving.data.network.coverage

import com.syntey.spiriteddriving.core.domain.coverage.Coverage
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.network.dto.coverage.CoverageDto
import com.syntey.spiriteddriving.data.network.dto.geo.MultiPolygonDto

internal fun CoverageDto.toDomain(): Coverage = Coverage(
    area = area.toDomain(),
    vignetteCountries = vignetteCountries.map { CountryCode(it) },
)

/** The first ring of each polygon is its outline, the rest are holes. */
internal fun MultiPolygonDto.toDomain(): List<GeoPolygon> = coordinates.map { rings ->
    GeoPolygon(
        outline = rings.first().toGeoPoints(),
        holes = rings.drop(1).map { it.toGeoPoints() },
    )
}

private fun List<List<Double>>.toGeoPoints(): List<GeoPoint> = map { position ->
    GeoPoint(latitude = position[1], longitude = position[0])
}
