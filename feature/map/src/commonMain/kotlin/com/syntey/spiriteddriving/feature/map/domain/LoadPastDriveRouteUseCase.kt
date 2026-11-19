package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.TripHistorySource
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.feature.map.tools.Constants
import org.koin.core.annotation.Factory

/**
 * The full route of the past drive with the ID: the one kept on the device, else the backend's, which is then kept. A drive is
 * never planned again, so the route is the one that was driven.
 */
@Factory
internal class LoadPastDriveRouteUseCase(private val tripHistorySource: TripHistorySource, private val tripHistoryCache: TripHistoryCache) :
    UseCase<String, Route> {

    override suspend fun build(args: String): Route = tripHistoryCache.getRoute(args)
        ?: tripHistorySource.getRoute(args, Constants.Trip.HAND_OFF_WAYPOINTS).also { tripHistoryCache.putRoute(args, it) }
}
