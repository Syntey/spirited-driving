package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import org.koin.core.annotation.Factory

/** Keeps the signed-in user's saved routes from the backend, in place of the kept list. Throws what the source throws. */
@Factory
internal class RefreshSavedRoutesUseCase(
    private val savedRoutesSource: SavedRoutesSource,
    private val savedRoutesCache: SavedRoutesCache,
) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) {
        savedRoutesCache.replaceAll(savedRoutesSource.list())
    }
}
