package com.syntey.spiriteddriving.data.persistence.persistence.place

import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toStored
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A recent place as kept on the device. Its shape is the stored format, so renaming a field loses the kept places.
 */
@Serializable
internal data class StoredPlace(
    @SerialName("name") val name: String,
    @SerialName("area") val area: String?,
    @SerialName("location") val location: StoredPoint,
)

internal fun Place.toStored() = StoredPlace(name = name, area = area, location = location.toStored())

internal fun StoredPlace.toDomain() = Place(name = name, area = area, location = location.toDomain())
