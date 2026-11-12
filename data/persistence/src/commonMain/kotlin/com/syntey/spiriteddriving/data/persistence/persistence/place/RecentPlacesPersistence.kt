package com.syntey.spiriteddriving.data.persistence.persistence.place

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.RecentPlaces
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import org.koin.core.annotation.Single

/**
 * Keeps the recent places as one JSON list, last used first. An unreadable list reads as empty.
 */
@Single
internal class RecentPlacesPersistence(private val jsonPersistence: JsonPersistence) : RecentPlaces {

    override suspend fun get(): List<Place> =
        jsonPersistence.getCatching<List<StoredPlace>>(KEY)?.map { it.toDomain() }.orEmpty()

    override suspend fun update(transform: (List<Place>) -> List<Place>): List<Place> =
        jsonPersistence.update<List<StoredPlace>>(KEY) { stored ->
            transform(stored?.map { it.toDomain() }.orEmpty()).map { it.toStored() }
        }.map { it.toDomain() }

    private companion object {
        val KEY = stringPreferencesKey("RECENT_PLACES")
    }
}
