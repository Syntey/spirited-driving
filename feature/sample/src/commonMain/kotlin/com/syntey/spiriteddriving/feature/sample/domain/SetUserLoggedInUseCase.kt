package com.syntey.spiriteddriving.feature.sample.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.sample.UserSession
import org.koin.core.annotation.Factory

@Factory
internal class SetUserLoggedInUseCase(private val userSession: UserSession) : UseCase<SetUserLoggedInUseCase.Args, Unit> {

    override suspend fun build(args: Args) = userSession.setUserLoggedIn(args.isLoggedIn)

    data class Args(val isLoggedIn: Boolean)
}
