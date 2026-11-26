package com.syntey.spiriteddriving.data.persistence.persistence.savedroute

import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.database.SavedRouteDetailEntity
import com.syntey.spiriteddriving.data.persistence.database.SavedRoutesDao
import com.syntey.spiriteddriving.data.persistence.database.toDomain
import com.syntey.spiriteddriving.data.persistence.database.toEntity
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceJson
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import com.syntey.spiriteddriving.data.persistence.persistence.route.StoredRoute
import com.syntey.spiriteddriving.data.persistence.persistence.route.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.route.toStored
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

/**
 * Keeps the signed-in user's saved routes in Room, one row per saved route with its position, and each full route as
 * JSON in its own table, with a meta row that marks a kept list. An unreadable full route reads as none.
 */
@Single
internal class SavedRoutesPersistence(
    private val jsonPersistence: JsonPersistence,
    private val database: PersistenceDatabase,
    private val savedRoutesDao: SavedRoutesDao,
    @PersistenceJson private val json: Json,
) : SavedRoutesCache {

    private val logger = Logger.withTag("SavedRoutesPersistence")

    // Only the list's tables: a change to a full route or to DataStore doesn't read the list again
    override fun observe(): Flow<List<SavedRoute>?> =
        database.invalidationTracker.createFlow(SAVED_ROUTE_TABLE, META_TABLE)
            .map { get() }
            .distinctUntilChanged()

    override suspend fun get(): List<SavedRoute>? = savedRoutesDao.read()?.map { it.toDomain() }

    override suspend fun replaceAll(routes: List<SavedRoute>) {
        savedRoutesDao.replaceAll(routes.mapIndexed { index, route -> route.toEntity(index) })
        removeLegacyKeys()
    }

    override suspend fun put(route: SavedRoute) {
        // The DAO picks the position: the route's place in the list, or the end of it
        savedRoutesDao.put(route.toEntity(position = 0))
    }

    override suspend fun remove(id: SavedRouteId) {
        savedRoutesDao.remove(id.value)
    }

    override suspend fun getRoute(id: SavedRouteId): Route? {
        val stored = savedRoutesDao.detail(id.value)?.route ?: return null
        return try {
            json.decodeFromString(StoredRoute.serializer(), stored).toDomain()
        } catch (error: SerializationException) {
            logger.e(error) { "Unable to decode the kept full route of saved route ${id.value}" }
            null
        }
    }

    override suspend fun putRoute(id: SavedRouteId, route: Route) {
        savedRoutesDao.upsertDetail(
            SavedRouteDetailEntity(
                savedRouteId = id.value,
                route = json.encodeToString(StoredRoute.serializer(), route.toStored()),
            ),
        )
    }

    override suspend fun clear() {
        savedRoutesDao.clear()
        removeLegacyKeys()
    }

    private suspend fun removeLegacyKeys() {
        jsonPersistence.delete(LEGACY_LIST_KEY)
        jsonPersistence.delete(LEGACY_DETAILS_KEY)
    }

    private companion object {
        const val SAVED_ROUTE_TABLE = "saved_route"
        const val META_TABLE = "saved_routes_meta"

        // The saved routes used to be kept under these keys. They're removed on the next write and never read: the
        // list is fetched again.
        val LEGACY_LIST_KEY = stringPreferencesKey("SAVED_ROUTES")
        val LEGACY_DETAILS_KEY = stringPreferencesKey("SAVED_ROUTE_DETAILS")
    }
}
