package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.platform.binding.UserLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Emits the user's location while it's collected, see [UserLocation].
 */
@Factory
internal class ObserveUserLocationUseCase(@Provided private val userLocation: UserLocation) : FlowUseCase<Unit, GeoPoint> {

    override fun build(args: Unit): Flow<GeoPoint> =
        userLocation.updates().map { GeoPoint(latitude = it.latitude, longitude = it.longitude) }
}
