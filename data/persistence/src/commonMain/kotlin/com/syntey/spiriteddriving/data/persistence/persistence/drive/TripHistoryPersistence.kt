package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import com.syntey.spiriteddriving.data.persistence.persistence.route.StoredRoute
import com.syntey.spiriteddriving.data.persistence.persistence.route.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.route.toStored
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Keeps the signed-in user's trip history as one JSON object, each drive's full route as a JSON map keyed by drive ID,
 * and the ratings not sent yet as a JSON map keyed by drive ID. Each drive's rating is what the list shows: the unsent
 * one when there is one, else the backend's. An unreadable copy reads as nothing kept.
 */
@Single
internal class TripHistoryPersistence(private val jsonPersistence: JsonPersistence) : TripHistoryCache {

    override fun observe(): Flow<PastDrivePage?> =
        jsonPersistence.observeCatching<StoredTripHistory>(LIST_KEY).map { stored -> stored?.toDomain() }

    override suspend fun get(): PastDrivePage? = jsonPersistence.getCatching<StoredTripHistory>(LIST_KEY)?.toDomain()

    override suspend fun replaceAll(page: PastDrivePage) {
        val unsent = unsentStored()
        jsonPersistence.save(
            LIST_KEY,
            StoredTripHistory(
                drives = page.drives.map { drive -> drive.toStored().withUnsent(unsent) },
                nextCursor = page.nextCursor,
            ),
        )
        val ids = page.drives.mapTo(mutableSetOf()) { it.id }
        jsonPersistence.update<Map<String, StoredRoute>>(ROUTES_KEY) { stored ->
            stored.orEmpty().filterKeys { it in ids }
        }
    }

    override suspend fun append(page: PastDrivePage) {
        val unsent = unsentStored()
        jsonPersistence.update<StoredTripHistory>(LIST_KEY) { stored ->
            val kept = stored?.drives.orEmpty()
            val keptIds = kept.mapTo(mutableSetOf()) { it.id }
            val added = page.drives.map { it.toStored() }.filterNot { it.id in keptIds }
            StoredTripHistory(
                drives = (kept + added).map { it.withUnsent(unsent) },
                nextCursor = page.nextCursor,
            )
        }
    }

    override suspend fun remove(id: String) {
        changeKept { kept -> kept.copy(drives = kept.drives.filterNot { it.id == id }) }
        jsonPersistence.update<Map<String, StoredRoute>>(ROUTES_KEY) { stored -> stored.orEmpty() - id }
        jsonPersistence.update<Map<String, StoredRating>>(UNSENT_KEY) { stored -> stored.orEmpty() - id }
    }

    override suspend fun rate(id: String, rating: Rating) {
        val kept = jsonPersistence.getCatching<StoredTripHistory>(LIST_KEY) ?: return
        if (kept.drives.none { it.id == id }) return
        jsonPersistence.update<Map<String, StoredRating>>(UNSENT_KEY) { stored ->
            stored.orEmpty() + (id to rating.toStored())
        }
        changeKept { current ->
            current.copy(drives = current.drives.map { if (it.id == id) it.copy(rating = rating.toStored()) else it })
        }
    }

    override suspend fun unsentRatings(): Map<String, Rating> =
        unsentStored().mapNotNull { (id, stored) -> stored.toDomain()?.let { id to it } }.toMap()

    override suspend fun markRatingSent(id: String, rating: Rating) {
        jsonPersistence.update<Map<String, StoredRating>>(UNSENT_KEY) { stored ->
            val unsent = stored.orEmpty()
            if (unsent[id] == rating.toStored()) unsent - id else unsent
        }
    }

    override suspend fun getRoute(id: String): Route? =
        jsonPersistence.getCatching<Map<String, StoredRoute>>(ROUTES_KEY)?.get(id)?.toDomain()

    override suspend fun putRoute(id: String, route: Route) {
        jsonPersistence.update<Map<String, StoredRoute>>(ROUTES_KEY) { stored ->
            stored.orEmpty() + (id to route.toStored())
        }
    }

    override suspend fun clear() {
        jsonPersistence.delete(LIST_KEY)
        jsonPersistence.delete(ROUTES_KEY)
        jsonPersistence.delete(UNSENT_KEY)
    }

    private suspend fun unsentStored(): Map<String, StoredRating> =
        jsonPersistence.getCatching<Map<String, StoredRating>>(UNSENT_KEY).orEmpty()

    /** Changes the kept list when there is one; nothing kept stays nothing kept. */
    private suspend fun changeKept(transform: (StoredTripHistory) -> StoredTripHistory) {
        jsonPersistence.getCatching<StoredTripHistory>(LIST_KEY) ?: return
        jsonPersistence.update<StoredTripHistory>(LIST_KEY) { transform(it ?: EMPTY) }
    }

    private fun StoredPastDrive.withUnsent(unsent: Map<String, StoredRating>): StoredPastDrive =
        copy(rating = unsent[id] ?: rating)

    private companion object {
        val LIST_KEY = stringPreferencesKey("TRIP_HISTORY")
        val ROUTES_KEY = stringPreferencesKey("TRIP_HISTORY_ROUTES")
        val UNSENT_KEY = stringPreferencesKey("TRIP_HISTORY_UNSENT_RATINGS")
        val EMPTY = StoredTripHistory(drives = emptyList(), nextCursor = null)
    }
}
