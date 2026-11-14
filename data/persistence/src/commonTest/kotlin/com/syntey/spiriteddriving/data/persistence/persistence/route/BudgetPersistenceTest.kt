package com.syntey.spiriteddriving.data.persistence.persistence.route

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

class BudgetPersistenceTest {

    @Test
    fun `nothing kept reads as no choice`() = runTest {
        assertNull(budgetPersistence().get())
    }

    @Test
    fun `a set choice reads back equal`() = runTest {
        val persistence = budgetPersistence()
        val choice = BudgetChoice(
            time = Budget.Time(45.minutes),
            distance = Budget.Distance(50.kilometres),
            kind = BudgetKind.Distance,
        )

        persistence.set(choice)

        assertEquals(choice, persistence.get())
    }

    @Test
    fun `an invalid copy reads as no choice`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[BUDGET_CHOICE_KEY] = "garbage" }

        assertNull(budgetPersistence(dataStore).get())
    }

    private fun budgetPersistence(dataStore: DataStore<Preferences> = InMemoryDataStore()) =
        BudgetPersistence(JsonPersistence(dataStore, PersistenceModule().provideJson()))

    private companion object {
        val BUDGET_CHOICE_KEY = stringPreferencesKey("BUDGET_CHOICE")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
