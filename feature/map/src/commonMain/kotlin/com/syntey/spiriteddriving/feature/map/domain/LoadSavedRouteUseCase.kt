package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import com.syntey.spiriteddriving.feature.map.tools.Constants
import org.koin.core.annotation.Factory

/**
 * The full route of the saved route with the ID: the one kept on the device, else the backend's, which is then kept. A saved
 * route is never planned again, so the route is the one that was saved.
 */
@Factory
internal class LoadSavedRouteUseCase(private val savedRoutesSource: SavedRoutesSource, private val savedRoutesCache: SavedRoutesCache) :
    UseCase<String, Route> {

    override suspend fun build(args: String): Route = savedRoutesCache.getRoute(args)
        ?: savedRoutesSource.getRoute(args, Constants.Trip.HAND_OFF_WAYPOINTS).also { savedRoutesCache.putRoute(args, it) }
}
