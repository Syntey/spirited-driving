package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Reads what the user allows for location now, without a system prompt.
 */
@Factory
internal class GetLocationAccessUseCase(@Provided private val locationPermission: LocationPermission) : UseCase<Unit, LocationAccess> {

    override suspend fun build(args: Unit): LocationAccess = locationPermission.current()
}
