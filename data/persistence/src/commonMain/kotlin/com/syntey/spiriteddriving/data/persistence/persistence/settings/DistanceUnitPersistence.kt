package com.syntey.spiriteddriving.data.persistence.persistence.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnitSettings
import com.syntey.spiriteddriving.data.persistence.persistence.PrimitivePersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Keeps the unit as a short code, so renaming the enum doesn't change what is stored. An unknown code reads as no choice.
 */
@Single
internal class DistanceUnitPersistence(private val primitivePersistence: PrimitivePersistence) : DistanceUnitSettings {

    override fun observe(): Flow<DistanceUnit?> = primitivePersistence.observeOrNull(KEY).map { stored ->
        when (stored) {
            KILOMETRES -> DistanceUnit.Kilometres
            MILES -> DistanceUnit.Miles
            else -> null
        }
    }

    override suspend fun set(unit: DistanceUnit) {
        primitivePersistence.save(KEY, if (unit == DistanceUnit.Miles) MILES else KILOMETRES)
    }

    private companion object {
        val KEY = stringPreferencesKey("DISTANCE_UNIT")
        const val KILOMETRES = "km"
        const val MILES = "mi"
    }
}
