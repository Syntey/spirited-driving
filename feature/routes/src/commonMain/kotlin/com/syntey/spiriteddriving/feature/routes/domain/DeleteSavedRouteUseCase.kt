package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import org.koin.core.annotation.Factory

/** Removes the saved route with the ID from the backend, then from the device. Throws what the source throws. */
@Factory
internal class DeleteSavedRouteUseCase(private val savedRoutesSource: SavedRoutesSource, private val savedRoutesCache: SavedRoutesCache) :
    UseCase<String, Unit> {

    override suspend fun build(args: String) {
        savedRoutesSource.delete(args)
        savedRoutesCache.remove(args)
    }
}
