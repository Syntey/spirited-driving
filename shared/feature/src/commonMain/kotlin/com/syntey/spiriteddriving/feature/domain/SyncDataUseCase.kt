package com.syntey.spiriteddriving.feature.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.network.rest.api.StarWarsApi
import com.syntey.spiriteddriving.network.rest.dto.Person
import com.syntey.spiriteddriving.network.rest.result.getOrThrow
import org.koin.core.annotation.Factory
import kotlin.random.Random

@Factory
internal class SyncDataUseCase(private val starWarsApi: StarWarsApi) : UseCase<Unit, Person> {

    override suspend fun build(args: Unit): Person = starWarsApi.getPerson(Random.nextInt(until = 100)).getOrThrow()
}
