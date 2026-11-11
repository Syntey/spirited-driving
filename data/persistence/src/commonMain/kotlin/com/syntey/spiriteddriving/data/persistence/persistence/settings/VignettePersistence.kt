package com.syntey.spiriteddriving.data.persistence.persistence.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import com.syntey.spiriteddriving.data.persistence.persistence.PrimitivePersistence
import org.koin.core.annotation.Single

/**
 * One key per country, so a country without a key is one the user hasn't answered for.
 */
@Single
internal class VignettePersistence(private val primitivePersistence: PrimitivePersistence) : VignetteSettings {

    override suspend fun hasVignette(country: CountryCode): Boolean? = primitivePersistence.get(key(country))

    override suspend fun setHasVignette(country: CountryCode, hasVignette: Boolean) {
        primitivePersistence.save(key(country), hasVignette)
    }

    private fun key(country: CountryCode) = booleanPreferencesKey("HAS_VIGNETTE_${country.value}")
}
