package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.data.persistence.database.PastDriveRouteEntity
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.database.TripHistoryDao
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
 * Keeps the signed-in user's trip history in Room: one row per drive with its position and its rating as JSON, and a
 * meta row that marks a kept history and holds the cursor of the page after it. Each full route is JSON in its own
 * table, and the ratings not sent yet are in another. The shown rating is the unsent one when there is one, else the
 * backend's, merged on write. An unreadable full route reads as none, an unreadable rating as no rating.
 */
@Single
internal class TripHistoryPersistence(
    private val jsonPersistence: JsonPersistence,
    private val database: PersistenceDatabase,
    private val tripHistoryDao: TripHistoryDao,
    @PersistenceJson private val json: Json,
) : TripHistoryCache {

    private val logger = Logger.withTag("TripHistoryPersistence")

    // Only the trip history's tables: a change to a full route or to an unsent rating doesn't read the page again
    override fun observe(): Flow<PastDrivePage?> =
        database.invalidationTracker.createFlow(PAST_DRIVE_TABLE, META_TABLE)
            .map { get() }
            .distinctUntilChanged()

    override suspend fun get(): PastDrivePage? {
        val kept = tripHistoryDao.read() ?: return null
        return PastDrivePage(
            drives = kept.drives.map { drive -> drive.toDomain(decodeRating(drive.rating)) },
            nextCursor = kept.nextCursor,
        )
    }

    override suspend fun replaceAll(page: PastDrivePage) {
        tripHistoryDao.replaceAll(
            drives = page.drives.mapIndexed { index, drive -> drive.toEntity(index, drive.rating?.let(::encodeRating)) },
            nextCursor = page.nextCursor,
        )
        removeLegacyKeys()
    }

    override suspend fun append(page: PastDrivePage) {
        // The DAO picks the positions: the drives go after the kept ones
        tripHistoryDao.append(
            drives = page.drives.map { drive -> drive.toEntity(position = 0, drive.rating?.let(::encodeRating)) },
            nextCursor = page.nextCursor,
        )
    }

    override suspend fun remove(id: String) {
        tripHistoryDao.remove(id)
    }

    override suspend fun rate(id: String, rating: Rating) {
        tripHistoryDao.rate(id, encodeRating(rating))
    }

    override suspend fun unsentRatings(): Map<String, Rating> =
        tripHistoryDao.unsentRatings().mapNotNull { unsent ->
            decodeRating(unsent.rating)?.let { rating -> unsent.driveId to rating }
        }.toMap()

    override suspend fun markRatingSent(id: String, rating: Rating) {
        // Rating.toStored() sorts the reasons, so the same rating always encodes to the same JSON
        tripHistoryDao.deleteUnsentRating(id, encodeRating(rating))
    }

    override suspend fun getRoute(id: String): Route? {
        val stored = tripHistoryDao.route(id)?.route ?: return null
        return try {
            json.decodeFromString(StoredRoute.serializer(), stored).toDomain()
        } catch (error: SerializationException) {
            logger.e(error) { "Unable to decode the kept full route of drive $id" }
            null
        }
    }

    override suspend fun putRoute(id: String, route: Route) {
        tripHistoryDao.upsertRoute(
            PastDriveRouteEntity(
                driveId = id,
                route = json.encodeToString(StoredRoute.serializer(), route.toStored()),
            ),
        )
    }

    override suspend fun clear() {
        tripHistoryDao.clear()
        removeLegacyKeys()
    }

    private fun encodeRating(rating: Rating): String =
        json.encodeToString(StoredRating.serializer(), rating.toStored())

    private fun decodeRating(stored: String?): Rating? {
        if (stored == null) return null
        return try {
            json.decodeFromString(StoredRating.serializer(), stored).toDomain()
        } catch (error: SerializationException) {
            logger.e(error) { "Unable to decode a kept rating" }
            null
        }
    }

    private suspend fun removeLegacyKeys() {
        jsonPersistence.delete(LEGACY_LIST_KEY)
        jsonPersistence.delete(LEGACY_ROUTES_KEY)
        jsonPersistence.delete(LEGACY_UNSENT_KEY)
    }

    private companion object {
        const val PAST_DRIVE_TABLE = "past_drive"
        const val META_TABLE = "trip_history_meta"

        // The trip history used to be kept under these keys. They're removed on the next write and never read: the
        // history is fetched again.
        val LEGACY_LIST_KEY = stringPreferencesKey("TRIP_HISTORY")
        val LEGACY_ROUTES_KEY = stringPreferencesKey("TRIP_HISTORY_ROUTES")
        val LEGACY_UNSENT_KEY = stringPreferencesKey("TRIP_HISTORY_UNSENT_RATINGS")
    }
}
