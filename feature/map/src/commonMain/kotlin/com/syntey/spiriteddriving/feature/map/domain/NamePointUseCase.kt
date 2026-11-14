package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSearch
import org.koin.core.annotation.Factory
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Names a point picked on the map: the nearest named place or address from the backend, or an unnamed place at the point
 * when none is near.
 */
@Factory
internal class NamePointUseCase(private val placeSearch: PlaceSearch) : UseCase<GeoPoint, Place> {

    override suspend fun build(args: GeoPoint): Place = placeSearch.placeAt(args) ?: unnamedPlace(args)
}

/** A place at [point] without a name, shown by its coordinates, such as "49.9394, 14.1882". */
internal fun unnamedPlace(point: GeoPoint): Place = Place(
    name = null,
    area = "${coordinateText(point.latitude)}, ${coordinateText(point.longitude)}",
    location = point,
)

// Four decimals, rounded half up; a minus sign only when the rounded value is below zero, so -0.00001 reads as "0.0000"
private fun coordinateText(value: Double): String {
    val tenThousandths = (abs(value) * DECIMAL_SCALE).roundToLong()
    val sign = if (value < 0 && tenThousandths != 0L) "-" else ""
    val whole = tenThousandths / DECIMAL_SCALE.toLong()
    val fraction = (tenThousandths % DECIMAL_SCALE.toLong()).toString().padStart(DECIMALS, '0')
    return "$sign$whole.$fraction"
}

private const val DECIMALS = 4
private const val DECIMAL_SCALE = 10_000.0
