package com.syntey.spiriteddriving.data.persistence.persistence.coverage

import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toStored
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One polygon of the covered area as kept on the device. Its shape is the stored format. */
@Serializable
internal data class StoredPolygon(
    @SerialName("outline") val outline: List<StoredPoint>,
    @SerialName("holes") val holes: List<List<StoredPoint>>,
)

internal fun GeoPolygon.toStored() = StoredPolygon(
    outline = outline.map { it.toStored() },
    holes = holes.map { hole -> hole.map { it.toStored() } },
)

internal fun StoredPolygon.toDomain() = GeoPolygon(
    outline = outline.map { it.toDomain() },
    holes = holes.map { hole -> hole.map { it.toDomain() } },
)
