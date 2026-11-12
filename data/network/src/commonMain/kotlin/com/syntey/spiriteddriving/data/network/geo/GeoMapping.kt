package com.syntey.spiriteddriving.data.network.geo

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto

internal fun GeoPointDto.toDomain(): GeoPoint = GeoPoint(latitude = lat, longitude = lon)
