package com.syntey.spiriteddriving.data.persistence.persistence.geo

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A [GeoPoint] as kept on the device. Its field names are the stored format. */
@Serializable
internal data class StoredPoint(@SerialName("lat") val latitude: Double, @SerialName("lon") val longitude: Double)

internal fun GeoPoint.toStored() = StoredPoint(latitude = latitude, longitude = longitude)

internal fun StoredPoint.toDomain() = GeoPoint(latitude = latitude, longitude = longitude)
