package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.appleMapsLink
import com.syntey.spiriteddriving.core.domain.drive.googleMapsLink
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.platform.binding.MapsApp
import com.syntey.spiriteddriving.core.platform.binding.MapsAppKind
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Opens [Route] in the phone's maps app: navigating it in Google Maps, or showing its directions in Apple Maps. Returns
 * [MapsAppResult.Missing] when the app is deleted or turned off.
 */
@Factory
internal class HandOffUseCase(@Provided private val mapsApp: MapsApp) : UseCase<Route, MapsAppResult> {

    override suspend fun build(args: Route): MapsAppResult {
        val link = when (mapsApp.kind) {
            MapsAppKind.GoogleMaps -> googleMapsLink(args)
            MapsAppKind.AppleMaps -> appleMapsLink(args)
        }
        return mapsApp.open(link)
    }
}
