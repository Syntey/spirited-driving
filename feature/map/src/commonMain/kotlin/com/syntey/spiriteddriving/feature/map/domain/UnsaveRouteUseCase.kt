package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import org.koin.core.annotation.Factory

/** Removes the saved route with the ID from the backend, then from the device. */
@Factory
internal class UnsaveRouteUseCase(private val savedRoutesSource: SavedRoutesSource, private val savedRoutesCache: SavedRoutesCache) :
    UseCase<SavedRouteId, Unit> {

    override suspend fun build(args: SavedRouteId) {
        savedRoutesSource.delete(args)
        savedRoutesCache.remove(args)
    }
}
