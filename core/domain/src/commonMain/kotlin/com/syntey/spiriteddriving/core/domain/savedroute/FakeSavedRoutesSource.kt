package com.syntey.spiriteddriving.core.domain.savedroute

import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import kotlin.time.Instant

/**
 * [SavedRoutesSource] that keeps [routes] in memory. A saved route's full route is the recorded plan's route with its
 * route ID, or its fun route. Set [error] to make every call throw it.
 */
class FakeSavedRoutesSource(routes: List<SavedRoute> = emptyList()) : SavedRoutesSource {

    var routes: List<SavedRoute> = routes
        private set

    var error: Throwable? = null

    val deleted: List<String>
        field = mutableListOf<String>()

    override suspend fun list(): List<SavedRoute> {
        error?.let { throw it }
        return routes
    }

    override suspend fun save(routeId: String, name: String): SavedRoute {
        error?.let { throw it }
        routes.firstOrNull { it.routeId == routeId }?.let { return it }
        val route = (listOf(FakeRoutePlanner.Recorded.fastest) + FakeRoutePlanner.Recorded.funRoutes)
            .firstOrNull { it.id == routeId } ?: FakeRoutePlanner.Recorded.funRoutes.first()
        val saved = route.toSavedRoute(id = "saved-${routes.size + 1}", name = name, savedAt = SAVED_AT)
        routes = routes + saved
        return saved
    }

    override suspend fun getRoute(id: String, maxHandOffWaypoints: Int): Route {
        error?.let { throw it }
        val saved = routes.firstOrNull { it.id == id } ?: throw SavedRouteNotFoundException()
        return (listOf(FakeRoutePlanner.Recorded.fastest) + FakeRoutePlanner.Recorded.funRoutes)
            .firstOrNull { it.id == saved.routeId } ?: FakeRoutePlanner.Recorded.funRoutes.first()
    }

    override suspend fun rename(id: String, name: String): SavedRoute {
        error?.let { throw it }
        val saved = routes.firstOrNull { it.id == id } ?: throw SavedRouteNotFoundException()
        val renamed = saved.copy(name = name.trim())
        routes = routes.map { if (it.id == id) renamed else it }
        return renamed
    }

    override suspend fun delete(id: String) {
        error?.let { throw it }
        deleted += id
        routes = routes.filterNot { it.id == id }
    }

    companion object {
        val SAVED_AT: Instant = Instant.parse("2026-11-08T09:30:00Z")

        /** The recorded plan's fun route, saved as "Karlštejn Castle". */
        val Recorded: SavedRoute = FakeRoutePlanner.Recorded.funRoutes.first()
            .toSavedRoute(id = "saved-1", name = "Karlštejn Castle", savedAt = SAVED_AT)
    }
}

/** This route as a saved route with [id] and [name]. */
fun Route.toSavedRoute(id: String, name: String, savedAt: Instant): SavedRoute = SavedRoute(
    id = id,
    name = name,
    savedAt = savedAt,
    routeId = this.id,
    start = start,
    destination = destination,
    duration = duration,
    distance = distance,
    funLevel = funLevel,
    stats = stats,
)
