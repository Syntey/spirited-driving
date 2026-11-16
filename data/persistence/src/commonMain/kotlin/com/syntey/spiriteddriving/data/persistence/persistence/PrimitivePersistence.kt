package com.syntey.spiriteddriving.data.persistence.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * [DataStore]-backed Persistence which allows storage and observing of persisted entities.
 */
@Single
internal class PrimitivePersistence(@Provided private val dataStore: DataStore<Preferences>) {

    suspend fun <T : Any> get(key: Preferences.Key<T>): T? = dataStore.data.firstOrNull()?.run {
        this[key]
    }

    fun <T : Any> observe(key: Preferences.Key<T>): Flow<T> = dataStore.data.mapNotNull {
        it[key]
    }

    /** Emits `null` while [key] is missing, and only when its value changes. */
    fun <T : Any> observeOrNull(key: Preferences.Key<T>): Flow<T?> = dataStore.data.map { it[key] }.distinctUntilChanged()

    /** Emits every stored preference, so a caller can pick the keys it owns. Emits again after each change. */
    fun observeAll(): Flow<Preferences> = dataStore.data

    suspend fun <T : Any> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    suspend fun delete(key: Preferences.Key<*>) = dataStore.edit {
        it.remove(key)
    }
}
