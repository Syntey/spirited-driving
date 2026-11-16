package com.syntey.spiriteddriving.data.persistence.persistence.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.persistence.persistence.PrimitivePersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VignettePersistenceTest {

    @Test
    fun `a country is unanswered until the user answers`() = runTest {
        val persistence = vignettePersistence()

        assertNull(persistence.hasVignette(CountryCode.Czechia))
    }

    @Test
    fun `both answers are kept`() = runTest {
        val persistence = vignettePersistence()

        persistence.setHasVignette(CountryCode.Czechia, hasVignette = false)
        assertEquals(false, persistence.hasVignette(CountryCode.Czechia))

        persistence.setHasVignette(CountryCode.Czechia, hasVignette = true)
        assertEquals(true, persistence.hasVignette(CountryCode.Czechia))
    }

    @Test
    fun `each country has its own answer`() = runTest {
        val persistence = vignettePersistence()

        persistence.setHasVignette(CountryCode.Czechia, hasVignette = true)

        assertNull(persistence.hasVignette(CountryCode("AT")))
    }

    @Test
    fun `no country has a vignette at first`() = runTest {
        val persistence = vignettePersistence()

        assertEquals(emptySet(), persistence.observeVignettes().first())
    }

    @Test
    fun `only the countries answered yes are observed`() = runTest {
        val persistence = vignettePersistence()

        persistence.setHasVignette(CountryCode.Czechia, hasVignette = true)
        persistence.setHasVignette(CountryCode("SK"), hasVignette = false)

        assertEquals(setOf(CountryCode.Czechia), persistence.observeVignettes().first())
    }

    @Test
    fun `a country whose vignette is taken away is no longer observed`() = runTest {
        val persistence = vignettePersistence()
        persistence.setHasVignette(CountryCode.Czechia, hasVignette = true)

        persistence.setHasVignette(CountryCode.Czechia, hasVignette = false)

        assertEquals(emptySet(), persistence.observeVignettes().first())
    }

    private fun vignettePersistence() = VignettePersistence(PrimitivePersistence(InMemoryDataStore()))

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
