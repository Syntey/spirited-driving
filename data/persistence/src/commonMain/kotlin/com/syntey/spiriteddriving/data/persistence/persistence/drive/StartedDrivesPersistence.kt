package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import com.syntey.spiriteddriving.data.persistence.tools.Constants
import org.koin.core.annotation.Single

/**
 * Keeps the started drives as one JSON list, newest first. An unreadable list reads as empty.
 */
@Single
internal class StartedDrivesPersistence(private val jsonPersistence: JsonPersistence) : StartedDrives {

    override suspend fun all(): List<Drive> =
        jsonPersistence.getCatching<List<StoredDrive>>(KEY)?.map { it.toDomain() }.orEmpty()

    override suspend fun add(drive: Drive) {
        jsonPersistence.update<List<StoredDrive>>(KEY) { stored ->
            (listOf(drive.toStored()) + stored.orEmpty().filterNot { it.id == drive.id })
                .take(Constants.Drives.MAX_KEPT)
        }
    }

    override suspend fun markRecorded(id: String) {
        jsonPersistence.update<List<StoredDrive>>(KEY) { stored ->
            stored.orEmpty().map { if (it.id == id) it.copy(isRecorded = true) else it }
        }
    }

    override suspend fun remove(id: String) {
        jsonPersistence.update<List<StoredDrive>>(KEY) { stored ->
            stored.orEmpty().filterNot { it.id == id }
        }
    }

    private companion object {
        val KEY = stringPreferencesKey("STARTED_DRIVES")
    }
}
