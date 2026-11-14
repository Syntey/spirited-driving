package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.MapsApp
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Opens the maps app's page in the app store, where the user can get the app or turn it on.
 */
@Factory
internal class OpenMapsAppStoreUseCase(@Provided private val mapsApp: MapsApp) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) = mapsApp.openStore()
}
