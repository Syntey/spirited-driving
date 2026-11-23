package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.Auth
import org.koin.core.annotation.Factory

/** Signs out of the account. The app keeps the device's settings and recent places. */
@Factory
internal class SignOutUseCase(private val auth: Auth) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) = auth.signOut()
}
