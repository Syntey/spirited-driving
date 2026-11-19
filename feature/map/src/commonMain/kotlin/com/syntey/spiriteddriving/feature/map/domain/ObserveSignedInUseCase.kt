package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.platform.binding.Auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/** Emits whether a user is signed in, now and after each sign-in or sign-out. */
@Factory
internal class ObserveSignedInUseCase(@Provided private val auth: Auth) : FlowUseCase<Unit, Boolean> {

    override fun build(args: Unit): Flow<Boolean> = auth.user.map { it != null }.distinctUntilChanged()
}
