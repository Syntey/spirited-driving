package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.SignInResult
import org.koin.core.annotation.Factory

/** Signs in with a provider's sheet and returns how it ended. */
@Factory
internal class SignInUseCase(private val auth: Auth) : UseCase<SignInProvider, SignInResult> {

    override suspend fun build(args: SignInProvider): SignInResult = auth.signIn(args)
}
