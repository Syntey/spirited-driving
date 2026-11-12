package com.syntey.spiriteddriving.data.network.dto.coverage

import com.syntey.spiriteddriving.data.network.dto.geo.MultiPolygonDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /coverage` (`Coverage` schema). */
@Serializable
internal data class CoverageDto(
    @SerialName("area") val area: MultiPolygonDto,
    @SerialName("vignetteCountries") val vignetteCountries: List<String>,
)
