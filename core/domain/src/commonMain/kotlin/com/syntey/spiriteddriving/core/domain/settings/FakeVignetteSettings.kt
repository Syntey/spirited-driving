package com.syntey.spiriteddriving.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * [VignetteSettings] that keeps the answers in memory. [answers] holds the countries the user answered for.
 */
class FakeVignetteSettings(answers: Map<CountryCode, Boolean> = emptyMap()) : VignetteSettings {

    private val answers = MutableStateFlow(answers)

    override suspend fun hasVignette(country: CountryCode): Boolean? = answers.value[country]

    override fun observeVignettes(): Flow<Set<CountryCode>> =
        answers.map { current -> current.filterValues { it }.keys }

    override suspend fun setHasVignette(country: CountryCode, hasVignette: Boolean) {
        answers.value = answers.value + (country to hasVignette)
    }
}
