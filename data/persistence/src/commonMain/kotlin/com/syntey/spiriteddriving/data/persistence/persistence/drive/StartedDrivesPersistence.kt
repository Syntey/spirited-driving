package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.data.persistence.database.StartedDrivesDao
import com.syntey.spiriteddriving.data.persistence.database.toDomain
import com.syntey.spiriteddriving.data.persistence.database.toEntity
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceJson
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import com.syntey.spiriteddriving.data.persistence.tools.Constants
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

/**
 * Keeps the started drives in Room: one row per drive with its rating as JSON, most recently added first. An unreadable
 * rating reads as no rating.
 */
@Single
internal class StartedDrivesPersistence(
    private val jsonPersistence: JsonPersistence,
    private val startedDrivesDao: StartedDrivesDao,
    @PersistenceJson private val json: Json,
) : StartedDrives {

    private val logger = Logger.withTag("StartedDrivesPersistence")

    override suspend fun all(): List<Drive> =
        startedDrivesDao.drives().map { drive -> drive.toDomain(decodeRating(drive.rating)) }

    override suspend fun add(drive: Drive) {
        // The DAO picks the position: the drive goes on top
        startedDrivesDao.add(
            drive.toEntity(position = 0, rating = drive.rating?.let(::encodeRating)),
            Constants.Drives.MAX_KEPT,
        )
        jsonPersistence.delete(LEGACY_KEY)
    }

    override suspend fun markRecorded(id: String) {
        startedDrivesDao.markRecorded(id)
    }

    override suspend fun remove(id: String) {
        startedDrivesDao.remove(id)
    }

    override suspend fun rate(id: String, rating: Rating) {
        startedDrivesDao.rate(id, encodeRating(rating))
    }

    override suspend fun markRatingSent(id: String, rating: Rating) {
        // Rating.toStored() sorts the reasons, so the same rating always encodes to the same JSON
        startedDrivesDao.markRatingSent(id, encodeRating(rating))
    }

    override suspend fun markNotDriven(id: String) {
        startedDrivesDao.markNotDriven(id)
    }

    private fun encodeRating(rating: Rating): String =
        json.encodeToString(StoredRating.serializer(), rating.toStored())

    private fun decodeRating(stored: String?): Rating? {
        if (stored == null) return null
        return try {
            json.decodeFromString(StoredRating.serializer(), stored).toDomain()
        } catch (error: SerializationException) {
            logger.e(error) { "Unable to decode the kept rating of a started drive" }
            null
        }
    }

    private companion object {
        // The started drives used to be kept under this key. It's removed on the next add and never read.
        val LEGACY_KEY = stringPreferencesKey("STARTED_DRIVES")
    }
}
