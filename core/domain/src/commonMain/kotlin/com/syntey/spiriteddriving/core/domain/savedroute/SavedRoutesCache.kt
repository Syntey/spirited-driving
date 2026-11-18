package com.syntey.spiriteddriving.core.domain.savedroute

import com.syntey.spiriteddriving.core.domain.route.Route
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's saved routes kept on the device, so the list shows at once and its routes can be driven offline.
 * Cleared on sign-out.
 */
interface SavedRoutesCache {

    /** The kept list, now and after each change; `null` while nothing was kept yet or it can't be read. */
    fun observe(): Flow<List<SavedRoute>?>

    /** The kept list, or `null` while nothing was kept yet or it can't be read. */
    suspend fun get(): List<SavedRoute>?

    /** Keeps [routes] in place of the kept list, and drops the full routes of saved routes no longer in it. */
    suspend fun replaceAll(routes: List<SavedRoute>)

    /** Adds [route], or replaces the kept one with its ID. Starts the list when nothing was kept yet. */
    suspend fun put(route: SavedRoute)

    /** Removes the saved route and its full route. */
    suspend fun remove(id: String)

    /** The full route kept for the saved route [id], or `null` when there's none or it can't be read. */
    suspend fun getRoute(id: String): Route?

    /** Keeps [route] as the full route of the saved route [id]. */
    suspend fun putRoute(id: String, route: Route)

    /** Removes everything kept. */
    suspend fun clear()
}
