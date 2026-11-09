package com.syntey.spiriteddriving.shared.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.sample.UserSession
import org.koin.core.annotation.Factory

@Factory
internal class IsUserLoggedInUseCase(private val userSession: UserSession) : UseCase<Unit, Boolean> {
    override suspend fun build(args: Unit): Boolean = userSession.isUserLoggedIn()
}
