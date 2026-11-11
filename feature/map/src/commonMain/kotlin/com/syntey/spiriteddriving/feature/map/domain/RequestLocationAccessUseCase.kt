package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Shows the system prompt for location access, and returns what the user allowed.
 */
@Factory
internal class RequestLocationAccessUseCase(@Provided private val locationPermission: LocationPermission) : UseCase<Unit, LocationAccess> {

    override suspend fun build(args: Unit): LocationAccess = locationPermission.request()
}
