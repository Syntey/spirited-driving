package com.syntey.spiriteddriving.feature.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.persistence.persistence.user.UserPersistence
import org.koin.core.annotation.Factory

@Factory
class IsUserLoggedInUseCase(private val userPersistence: UserPersistence) : UseCase<Unit, Boolean> {
    override suspend fun build(args: Unit): Boolean = userPersistence.isUserLoggedIn()
}
