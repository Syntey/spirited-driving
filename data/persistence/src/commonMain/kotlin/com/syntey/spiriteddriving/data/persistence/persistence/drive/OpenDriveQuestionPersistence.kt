package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import org.koin.core.annotation.Single

/**
 * Keeps the open drive question as one JSON object. An unreadable question reads as none.
 */
@Single
internal class OpenDriveQuestionPersistence(private val jsonPersistence: JsonPersistence) : OpenDriveQuestion {

    override suspend fun get(): DriveQuestion? =
        jsonPersistence.getCatching<StoredDriveQuestion>(KEY)?.toDomain()

    override suspend fun set(question: DriveQuestion) {
        jsonPersistence.save(KEY, question.toStored())
    }

    override suspend fun clear(driveId: String) {
        jsonPersistence.deleteIf<StoredDriveQuestion>(KEY) { it.driveId == driveId }
    }

    private companion object {
        val KEY = stringPreferencesKey("OPEN_DRIVE_QUESTION")
    }
}
