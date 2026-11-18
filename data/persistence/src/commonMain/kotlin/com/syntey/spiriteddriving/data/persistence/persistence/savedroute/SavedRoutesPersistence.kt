package com.syntey.spiriteddriving.data.persistence.persistence.savedroute

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import com.syntey.spiriteddriving.data.persistence.persistence.route.StoredRoute
import com.syntey.spiriteddriving.data.persistence.persistence.route.toStored
import com.syntey.spiriteddriving.data.persistence.persistence.route.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Keeps the signed-in user's saved routes as one JSON list, and each one's full route as a JSON map keyed by saved
 * route ID. An unreadable copy reads as nothing kept.
 */
@Single
internal class SavedRoutesPersistence(private val jsonPersistence: JsonPersistence) : SavedRoutesCache {

    override fun observe(): Flow<List<SavedRoute>?> =
        jsonPersistence.observeCatching<List<StoredSavedRoute>>(LIST_KEY).map { stored ->
            stored?.map { it.toDomain() }
        }

    override suspend fun get(): List<SavedRoute>? =
        jsonPersistence.getCatching<List<StoredSavedRoute>>(LIST_KEY)?.map { it.toDomain() }

    override suspend fun replaceAll(routes: List<SavedRoute>) {
        jsonPersistence.save(LIST_KEY, routes.map { it.toStored() })
        val ids = routes.mapTo(mutableSetOf()) { it.id }
        jsonPersistence.update<Map<String, StoredRoute>>(DETAILS_KEY) { stored ->
            stored.orEmpty().filterKeys { it in ids }
        }
    }

    override suspend fun put(route: SavedRoute) {
        jsonPersistence.update<List<StoredSavedRoute>>(LIST_KEY) { stored ->
            val current = stored.orEmpty()
            val updated = route.toStored()
            if (current.any { it.id == route.id }) {
                current.map { if (it.id == route.id) updated else it }
            } else {
                current + updated
            }
        }
    }

    override suspend fun remove(id: String) {
        jsonPersistence.update<List<StoredSavedRoute>>(LIST_KEY) { stored ->
            stored.orEmpty().filterNot { it.id == id }
        }
        jsonPersistence.update<Map<String, StoredRoute>>(DETAILS_KEY) { stored ->
            stored.orEmpty() - id
        }
    }

    override suspend fun getRoute(id: String): Route? =
        jsonPersistence.getCatching<Map<String, StoredRoute>>(DETAILS_KEY)?.get(id)?.toDomain()

    override suspend fun putRoute(id: String, route: Route) {
        jsonPersistence.update<Map<String, StoredRoute>>(DETAILS_KEY) { stored ->
            stored.orEmpty() + (id to route.toStored())
        }
    }

    override suspend fun clear() {
        jsonPersistence.delete(LIST_KEY)
        jsonPersistence.delete(DETAILS_KEY)
    }

    private companion object {
        val LIST_KEY = stringPreferencesKey("SAVED_ROUTES")
        val DETAILS_KEY = stringPreferencesKey("SAVED_ROUTE_DETAILS")
    }
}
