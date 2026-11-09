package com.syntey.spiriteddriving.data.network.api

import com.syntey.spiriteddriving.core.domain.sample.Person
import com.syntey.spiriteddriving.core.domain.sample.PersonRepository
import com.syntey.spiriteddriving.data.network.result.getOrThrow
import org.koin.core.annotation.Factory

@Factory
internal class ApiPersonRepository(private val starWarsApi: StarWarsApi) : PersonRepository {

    override suspend fun getPerson(id: Int): Person {
        val person = starWarsApi.getPerson(id).getOrThrow()
        return Person(name = person.name.orEmpty())
    }
}
