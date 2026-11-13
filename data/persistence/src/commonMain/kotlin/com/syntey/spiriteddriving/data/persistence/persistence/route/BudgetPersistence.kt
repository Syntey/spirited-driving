package com.syntey.spiriteddriving.data.persistence.persistence.route

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetSettings
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import org.koin.core.annotation.Single

/**
 * Keeps the budget choice as one JSON object. An unreadable one reads as no choice.
 */
@Single
internal class BudgetPersistence(private val jsonPersistence: JsonPersistence) : BudgetSettings {

    override suspend fun get(): BudgetChoice? = jsonPersistence.getCatching<StoredBudgetChoice>(KEY)?.toDomain()

    override suspend fun set(choice: BudgetChoice) {
        jsonPersistence.save(KEY, choice.toStored())
    }

    private companion object {
        val KEY = stringPreferencesKey("BUDGET_CHOICE")
    }
}
