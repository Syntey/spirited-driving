package com.syntey.spiriteddriving.feature.sample.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.sample.Person
import com.syntey.spiriteddriving.core.domain.sample.PersonRepository
import org.koin.core.annotation.Factory
import kotlin.random.Random

@Factory
internal class SyncDataUseCase(private val personRepository: PersonRepository) : UseCase<Unit, Person> {

    override suspend fun build(args: Unit): Person = personRepository.getPerson(Random.nextInt(until = 100))
}
