package com.syntey.spiriteddriving.core.domain.sample

/**
 * Characters of the template's sample API, used by `:feature:sample`.
 */
interface PersonRepository {
    suspend fun getPerson(id: Int): Person
}

data class Person(val name: String)
