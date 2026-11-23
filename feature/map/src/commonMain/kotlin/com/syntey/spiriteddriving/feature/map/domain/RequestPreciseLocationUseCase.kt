package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import org.koin.core.annotation.Factory

/**
 * Offers precise location while only approximate is allowed, and returns what the user allows.
 */
@Factory
internal class RequestPreciseLocationUseCase(private val locationPermission: LocationPermission) : UseCase<Unit, LocationAccess> {

    override suspend fun build(args: Unit): LocationAccess = locationPermission.requestPrecise()
}
