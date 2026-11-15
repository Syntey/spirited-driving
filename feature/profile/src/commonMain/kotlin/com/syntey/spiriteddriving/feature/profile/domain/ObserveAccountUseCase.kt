package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/** Emits the signed-in user, or null when signed out: now, and after each sign-in or sign-out. */
@Factory
internal class ObserveAccountUseCase(@Provided private val auth: Auth) : FlowUseCase<Unit, AuthUser?> {

    override fun build(args: Unit): Flow<AuthUser?> = auth.user
}
