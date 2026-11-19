package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

/** Emits the kept saved routes, now and after each change; empty while nothing is kept. */
@Factory
internal class ObserveSavedRoutesUseCase(private val savedRoutesCache: SavedRoutesCache) : FlowUseCase<Unit, List<SavedRoute>> {

    override fun build(args: Unit): Flow<List<SavedRoute>> = savedRoutesCache.observe().map { it.orEmpty() }
}
