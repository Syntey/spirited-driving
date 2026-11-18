package com.syntey.spiriteddriving.core.domain.savedroute

import com.syntey.spiriteddriving.core.domain.route.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [SavedRoutesCache] that keeps everything in memory.
 */
class FakeSavedRoutesCache(routes: List<SavedRoute>? = null, fullRoutes: Map<String, Route> = emptyMap()) : SavedRoutesCache {

    private val list = MutableStateFlow(routes)

    var fullRoutes: Map<String, Route> = fullRoutes
        private set

    override fun observe(): Flow<List<SavedRoute>?> = list

    override suspend fun get(): List<SavedRoute>? = list.value

    override suspend fun replaceAll(routes: List<SavedRoute>) {
        list.value = routes
        val ids = routes.mapTo(mutableSetOf()) { it.id }
        fullRoutes = fullRoutes.filterKeys { it in ids }
    }

    override suspend fun put(route: SavedRoute) {
        val current = list.value.orEmpty()
        list.value = if (current.any { it.id == route.id }) {
            current.map { if (it.id == route.id) route else it }
        } else {
            current + route
        }
    }

    override suspend fun remove(id: String) {
        list.value = list.value?.filterNot { it.id == id }
        fullRoutes = fullRoutes - id
    }

    override suspend fun getRoute(id: String): Route? = fullRoutes[id]

    override suspend fun putRoute(id: String, route: Route) {
        fullRoutes = fullRoutes + (id to route)
    }

    override suspend fun clear() {
        list.value = null
        fullRoutes = emptyMap()
    }
}
