package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import org.koin.core.annotation.Factory

/** Gives the saved route a new name on the backend, then on the device. Throws what the source throws. */
@Factory
internal class RenameSavedRouteUseCase(private val savedRoutesSource: SavedRoutesSource, private val savedRoutesCache: SavedRoutesCache) :
    UseCase<RenameSavedRouteUseCase.Args, Unit> {

    data class Args(val id: String, val name: String)

    override suspend fun build(args: Args) {
        savedRoutesCache.put(savedRoutesSource.rename(args.id, args.name.trim()))
    }
}
