package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Opens the system settings, where the user can turn location access on after denying it for good.
 */
@Factory
internal class OpenLocationSettingsUseCase(@Provided private val locationPermission: LocationPermission) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) = locationPermission.openSettings()
}
