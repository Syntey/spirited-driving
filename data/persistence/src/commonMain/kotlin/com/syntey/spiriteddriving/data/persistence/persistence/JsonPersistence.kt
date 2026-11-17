package com.syntey.spiriteddriving.data.persistence.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceJson
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * [DataStore]-backed Persistence which allows storage and observing of complex JSON objects.
 * Uses [kotlinx.serialization] to serialize and deserialize objects into Strings.
 */
@Single
internal class JsonPersistence(@Provided private val dataStore: DataStore<Preferences>, @PersistenceJson private val json: Json) {

    private val logger = Logger.withTag("JsonPersistence")

    /**
     * Saves provided [value] in persistence under provided [key].
     */
    suspend inline fun <reified T : Any> save(
        key: Preferences.Key<String>,
        value: T,
    ) {
        dataStore.edit { preferences -> preferences[key] = json.encodeToString<T>(value) }
    }

    /**
     * Returns persisted object with provided [key].
     *
     * @return Persisted object, or `null` if does not exist.
     * @throws [SerializationException] if object could not be deserialized as [T].
     */
    suspend inline fun <reified T : Any> get(key: Preferences.Key<String>): T? =
        dataStore.data.firstOrNull()
            ?.let { preferences -> preferences[key] }
            ?.let { jsonString ->
                json.decodeFromString<T>(jsonString)
            }

    /**
     * Returns a [Flow] of persisted objects with provided [key].
     *
     * @return [Flow] of objects. Values in [Flow] can be `null` if not found in persistence.
     * @throws [SerializationException] if object could not be deserialized as [T].
     */
    inline fun <reified T : Any> observe(key: Preferences.Key<String>): Flow<T?> =
        dataStore.data
            .map { preferences -> preferences[key] }
            .map { jsonString ->
                jsonString?.let { json.decodeFromString(jsonString) }
            }

    /**
     * Replaces the object under [key] with what [transform] makes of it, in one edit. An object that can't be read as [T]
     * reaches [transform] as `null`.
     *
     * @return The object [transform] made.
     */
    suspend inline fun <reified T : Any> update(
        key: Preferences.Key<String>,
        crossinline transform: (T?) -> T,
    ): T {
        var updated: T? = null
        dataStore.edit { preferences ->
            val current = preferences[key]?.let { jsonString ->
                runCatching { json.decodeFromString<T>(jsonString) }
                    .recoverCatching { error ->
                        if (error is SerializationException) {
                            logger.e(error) { "Unable to deserialize property with key '${key.name}'" }
                            null
                        } else {
                            throw error
                        }
                    }.getOrThrow()
            }
            val value = transform(current)
            preferences[key] = json.encodeToString<T>(value)
            updated = value
        }
        return requireNotNull(updated)
    }

    /**
     * Removes the persisted object under [key] if it can be read as [T] and [predicate] holds for it, in one edit. An
     * unreadable object is kept.
     */
    suspend inline fun <reified T : Any> deleteIf(
        key: Preferences.Key<String>,
        crossinline predicate: (T) -> Boolean,
    ) {
        dataStore.edit { preferences ->
            val jsonString = preferences[key] ?: return@edit
            val current = try {
                json.decodeFromString<T>(jsonString)
            } catch (error: SerializationException) {
                logger.e(error) { "Unable to deserialize property with key '${key.name}'" }
                return@edit
            }
            if (predicate(current)) preferences.remove(key)
        }
    }

    /**
     * Removes persisted object with provided [key].
     */
    suspend fun delete(key: Preferences.Key<String>) = dataStore.edit { preferences -> preferences.remove(key) }

    /**
     * Returns persisted object with provided [key], catching any [SerializationException]s and mapping them to `null`.
     *
     * @return Persisted object, or `null` if does not exist or object cannot be deserialized as [T].
     */
    suspend inline fun <reified T : Any> getCatching(key: Preferences.Key<String>): T? =
        runCatching { get<T>(key) }
            .recoverCatching { error ->
                if (error is SerializationException) {
                    logger.e(error) { "Unable to deserialize property with key '${key.name}'" }
                    null
                } else {
                    throw error
                }
            }
            .getOrThrow()

    /**
     * Returns a [Flow] of persisted objects with provided [key], catching any [SerializationException]s that might occur
     * and mapping them to `null` values.
     *
     * @return [Flow] of objects. Values in [Flow] can be `null` if not found in persistence or cannot be deserialized as [T].
     */
    inline fun <reified T : Any> observeCatching(key: Preferences.Key<String>): Flow<T?> =
        dataStore.data
            .map { preferences -> preferences[key] }
            .map { jsonString ->
                runCatching { jsonString?.let { json.decodeFromString<T>(jsonString) } }
                    .recoverCatching { error ->
                        if (error is SerializationException) {
                            logger.e(error) { "Unable to deserialize property with key '${key.name}'" }
                            null
                        } else {
                            null
                        }
                    }.getOrThrow()
            }
}
