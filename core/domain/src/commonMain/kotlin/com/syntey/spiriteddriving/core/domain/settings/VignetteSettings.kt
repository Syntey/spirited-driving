package com.syntey.spiriteddriving.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmInline

/**
 * Which countries the user has a valid motorway vignette for, so routes may use the motorways that need one. Kept on the
 * device only. First launch asks about Czechia; Settings changes it.
 */
interface VignetteSettings {

    /**
     * Whether the user has a vignette for [country], or `null` while they haven't answered.
     */
    suspend fun hasVignette(country: CountryCode): Boolean?

    /** The countries the user has a vignette for, now and after each change. */
    fun observeVignettes(): Flow<Set<CountryCode>>

    suspend fun setHasVignette(country: CountryCode, hasVignette: Boolean)
}

/**
 * A country's ISO 3166-1 alpha-2 code, such as `CZ`.
 */
@JvmInline
value class CountryCode(val value: String) {
    companion object {
        val Czechia = CountryCode("CZ")
    }
}
