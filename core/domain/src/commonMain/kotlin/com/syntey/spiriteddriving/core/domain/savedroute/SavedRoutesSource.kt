package com.syntey.spiriteddriving.core.domain.savedroute

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.Route

/**
 * The signed-in user's saved routes on the backend. Every call throws [OfflineException] without a connection.
 */
interface SavedRoutesSource {

    /** All of the user's saved routes, in no particular order. */
    suspend fun list(): List<SavedRoute>

    /** Saves the planned route [routeId] as [name]. Saving a route that's already saved returns the existing one. */
    suspend fun save(routeId: String, name: String): SavedRoute

    /**
     * The saved route's full route, with a hand-off for at most [maxHandOffWaypoints] waypoints. Throws
     * [SavedRouteNotFoundException] when it's gone, such as deleted on another phone.
     */
    suspend fun getRoute(id: String, maxHandOffWaypoints: Int): Route

    /** Gives the saved route [name], trimmed by the backend. Throws [SavedRouteNotFoundException] when it's gone. */
    suspend fun rename(id: String, name: String): SavedRoute

    /** Removes the saved route. Deleting one that's already gone succeeds. */
    suspend fun delete(id: String)
}
